package br.com.fiap.alertacheia.service;

import br.com.fiap.alertacheia.dto.AlertaDtos.AlertaResponse;
import br.com.fiap.alertacheia.dto.LeituraDtos.*;
import br.com.fiap.alertacheia.exception.BusinessException;
import br.com.fiap.alertacheia.exception.NotFoundException;
import br.com.fiap.alertacheia.exception.UnauthorizedException;
import br.com.fiap.alertacheia.model.*;
import br.com.fiap.alertacheia.repository.AlertaRepository;
import br.com.fiap.alertacheia.repository.BairroRepository;
import br.com.fiap.alertacheia.repository.LeituraRepository;
import br.com.fiap.alertacheia.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;

/**
 * Coracao do sistema: recebe a leitura do sensor (IoT), salva e decide se gera um alerta.
 *
 * Regras:
 *  - valor >= limiteCritico -> alerta CRITICO
 *  - valor >= limiteAlerta  -> alerta ATENCAO
 *  - nao duplica: so cria alerta se o bairro nao tiver um ATIVO de severidade igual ou maior
 *  - ao abrir um CRITICO, os alertas de ATENCAO ativos do bairro sao encerrados (o CRITICO substitui)
 */
@Service
public class LeituraService {

    private final LeituraRepository leituraRepository;
    private final SensorRepository sensorRepository;
    private final AlertaRepository alertaRepository;
    private final BairroRepository bairroRepository;
    private final byte[] chaveSensores;

    public LeituraService(LeituraRepository leituraRepository, SensorRepository sensorRepository,
                          AlertaRepository alertaRepository, BairroRepository bairroRepository,
                          @Value("${app.sensor.api-key}") String chaveSensores) {
        this.leituraRepository = leituraRepository;
        this.sensorRepository = sensorRepository;
        this.alertaRepository = alertaRepository;
        this.bairroRepository = bairroRepository;
        this.chaveSensores = chaveSensores.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public LeituraResponse registrar(String chaveRecebida, LeituraRequest req) {
        validarChave(chaveRecebida);

        Sensor sensor = sensorRepository.findByCodigo(req.codigoSensor())
                .orElseThrow(() -> new NotFoundException("Sensor " + req.codigoSensor() + " nao encontrado"));
        if (!sensor.isAtivo()) {
            throw new BusinessException("Sensor " + sensor.getCodigo() + " esta inativo");
        }

        Leitura leitura = leituraRepository.save(new Leitura(sensor, req.valor()));
        Severidade severidade = classificar(sensor, req.valor());
        Alerta alerta = severidade == null ? null : gerarAlertaSeNecessario(sensor, leitura, severidade);

        String situacao = severidade == null ? "NORMAL" : severidade.name();
        return LeituraResponse.from(leitura, situacao, alerta == null ? null : AlertaResponse.from(alerta));
    }

    @Transactional(readOnly = true)
    public List<LeituraResponse> ultimas(Long sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new NotFoundException("Sensor " + sensorId + " nao encontrado");
        }
        return leituraRepository.findTop50BySensorIdOrderByDataHoraDesc(sensorId).stream()
                .map(l -> {
                    Severidade s = classificar(l.getSensor(), l.getValor());
                    return LeituraResponse.from(l, s == null ? "NORMAL" : s.name(), null);
                })
                .toList();
    }

    /** Publico (sem banco) para facilitar o teste de unidade. */
    public static Severidade classificar(Sensor sensor, BigDecimal valor) {
        if (valor.compareTo(sensor.getLimiteCritico()) >= 0) return Severidade.CRITICO;
        if (valor.compareTo(sensor.getLimiteAlerta()) >= 0) return Severidade.ATENCAO;
        return null;
    }

    private Alerta gerarAlertaSeNecessario(Sensor sensor, Leitura leitura, Severidade severidade) {
        Bairro bairro = bairroRepository.travarPorId(sensor.getBairro().getId())
                .orElseThrow(() -> new NotFoundException("Bairro nao encontrado"));
        Set<Severidade> igualOuMaior = severidade == Severidade.CRITICO
                ? Set.of(Severidade.CRITICO) : Set.of(Severidade.ATENCAO, Severidade.CRITICO);
        if (alertaRepository.existsByBairroIdAndStatusAndSeveridadeIn(bairro.getId(), StatusAlerta.ATIVO, igualOuMaior)) {
            return null; // ja existe alerta equivalente ativo: nao "spama" o morador
        }
        if (severidade == Severidade.CRITICO) {
            alertaRepository.findByBairroIdAndStatus(bairro.getId(), StatusAlerta.ATIVO)
                    .forEach(a -> a.setStatus(StatusAlerta.ENCERRADO));
        }
        return alertaRepository.save(new Alerta(bairro, leitura, severidade, montarMensagem(sensor, leitura, severidade)));
    }

    private String montarMensagem(Sensor sensor, Leitura leitura, Severidade severidade) {
        String medida = sensor.getTipo() == TipoSensor.NIVEL_RIO ? "Nivel do rio" : "Chuva";
        String acao = severidade == Severidade.CRITICO
                ? "Risco alto de alagamento. Procure um abrigo ou local elevado."
                : "Fique atento e evite areas proximas ao corrego.";
        return "%s em %s %s no bairro %s. %s".formatted(medida, leitura.getValor().toPlainString(), sensor.unidade(),
                sensor.getBairro().getNome(), acao);
    }

    /** Compara em tempo constante para nao vazar a chave por diferenca de tempo de resposta. */
    private void validarChave(String recebida) {
        if (recebida == null || !MessageDigest.isEqual(chaveSensores, recebida.getBytes(StandardCharsets.UTF_8))) {
            throw new UnauthorizedException("Chave do sensor (X-Sensor-Key) ausente ou invalida");
        }
    }
}
