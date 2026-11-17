package br.com.fiap.alertacheia.service;

import br.com.fiap.alertacheia.dto.AlertaDtos.AlertaResponse;
import br.com.fiap.alertacheia.dto.BairroDtos.*;
import br.com.fiap.alertacheia.exception.ConflictException;
import br.com.fiap.alertacheia.exception.NotFoundException;
import br.com.fiap.alertacheia.model.Alerta;
import br.com.fiap.alertacheia.model.Bairro;
import br.com.fiap.alertacheia.model.Severidade;
import br.com.fiap.alertacheia.model.StatusAlerta;
import br.com.fiap.alertacheia.repository.AlertaRepository;
import br.com.fiap.alertacheia.repository.BairroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BairroService {

    private final BairroRepository bairroRepository;
    private final AlertaRepository alertaRepository;

    public BairroService(BairroRepository bairroRepository, AlertaRepository alertaRepository) {
        this.bairroRepository = bairroRepository;
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public List<BairroResponse> listar() {
        return bairroRepository.findAll(Sort.by("nome")).stream().map(BairroResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BairroResponse buscar(Long id) {
        return BairroResponse.from(buscarEntidade(id));
    }

    /** Situacao atual do bairro: o pior alerta ativo define a cor do card no app. */
    @Transactional(readOnly = true)
    public SituacaoBairroResponse situacao(Long id) {
        Bairro bairro = buscarEntidade(id);
        List<Alerta> ativos = alertaRepository.findByBairroIdAndStatus(id, StatusAlerta.ATIVO);
        String situacao = ativos.stream().anyMatch(a -> a.getSeveridade() == Severidade.CRITICO) ? "CRITICO"
                : ativos.isEmpty() ? "NORMAL" : "ATENCAO";
        return new SituacaoBairroResponse(BairroResponse.from(bairro), situacao,
                ativos.stream().map(AlertaResponse::from).toList());
    }

    @Transactional
    public BairroResponse criar(BairroRequest req) {
        if (bairroRepository.existsByNomeIgnoreCaseAndCidadeIgnoreCase(req.nome().trim(), req.cidade().trim())) {
            throw new ConflictException("Bairro ja cadastrado nesta cidade");
        }
        return BairroResponse.from(bairroRepository.save(new Bairro(req.nome().trim(), req.cidade().trim(), req.nivelRisco())));
    }

    @Transactional
    public BairroResponse atualizar(Long id, BairroRequest req) {
        Bairro bairro = buscarEntidade(id);
        bairro.setNome(req.nome().trim());
        bairro.setCidade(req.cidade().trim());
        bairro.setNivelRisco(req.nivelRisco());
        return BairroResponse.from(bairro);
    }

    @Transactional
    public void excluir(Long id) {
        bairroRepository.delete(buscarEntidade(id));
        bairroRepository.flush(); // forca o DELETE agora: se houver vinculos, o handler devolve 409
    }

    Bairro buscarEntidade(Long id) {
        return bairroRepository.findById(id).orElseThrow(() -> new NotFoundException("Bairro " + id + " nao encontrado"));
    }
}
