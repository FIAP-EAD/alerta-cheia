package br.com.fiap.alertacheia.service;

import br.com.fiap.alertacheia.dto.SensorDtos.*;
import br.com.fiap.alertacheia.exception.BusinessException;
import br.com.fiap.alertacheia.exception.ConflictException;
import br.com.fiap.alertacheia.exception.NotFoundException;
import br.com.fiap.alertacheia.model.Sensor;
import br.com.fiap.alertacheia.repository.SensorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private final BairroService bairroService;

    public SensorService(SensorRepository sensorRepository, BairroService bairroService) {
        this.sensorRepository = sensorRepository;
        this.bairroService = bairroService;
    }

    @Transactional(readOnly = true)
    public List<SensorResponse> listar(Long bairroId) {
        List<Sensor> sensores = bairroId == null ? sensorRepository.findAll(Sort.by("codigo"))
                : sensorRepository.findByBairroId(bairroId);
        return sensores.stream().map(SensorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SensorResponse buscar(Long id) {
        return SensorResponse.from(buscarEntidade(id));
    }

    @Transactional
    public SensorResponse criar(SensorRequest req) {
        validarLimites(req);
        if (sensorRepository.existsByCodigo(req.codigo())) {
            throw new ConflictException("Ja existe um sensor com o codigo " + req.codigo());
        }
        Sensor sensor = new Sensor(req.codigo(), req.tipo(), bairroService.buscarEntidade(req.bairroId()),
                req.limiteAlerta(), req.limiteCritico());
        if (req.ativo() != null) sensor.setAtivo(req.ativo());
        return SensorResponse.from(sensorRepository.save(sensor));
    }

    @Transactional
    public SensorResponse atualizar(Long id, SensorRequest req) {
        validarLimites(req);
        Sensor sensor = buscarEntidade(id);
        if (!sensor.getCodigo().equals(req.codigo()) && sensorRepository.existsByCodigo(req.codigo())) {
            throw new ConflictException("Ja existe um sensor com o codigo " + req.codigo());
        }
        sensor.setCodigo(req.codigo());
        sensor.setTipo(req.tipo());
        sensor.setBairro(bairroService.buscarEntidade(req.bairroId()));
        sensor.setLimiteAlerta(req.limiteAlerta());
        sensor.setLimiteCritico(req.limiteCritico());
        if (req.ativo() != null) sensor.setAtivo(req.ativo());
        return SensorResponse.from(sensor);
    }

    @Transactional
    public void excluir(Long id) {
        sensorRepository.delete(buscarEntidade(id));
        sensorRepository.flush();
    }

    Sensor buscarEntidade(Long id) {
        return sensorRepository.findById(id).orElseThrow(() -> new NotFoundException("Sensor " + id + " nao encontrado"));
    }

    private void validarLimites(SensorRequest req) {
        if (req.limiteCritico().compareTo(req.limiteAlerta()) <= 0) {
            throw new BusinessException("limiteCritico deve ser maior que limiteAlerta");
        }
    }
}
