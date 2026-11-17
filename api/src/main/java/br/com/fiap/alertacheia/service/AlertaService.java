package br.com.fiap.alertacheia.service;

import br.com.fiap.alertacheia.dto.AlertaDtos.AlertaResponse;
import br.com.fiap.alertacheia.exception.ConflictException;
import br.com.fiap.alertacheia.exception.NotFoundException;
import br.com.fiap.alertacheia.model.Alerta;
import br.com.fiap.alertacheia.model.StatusAlerta;
import br.com.fiap.alertacheia.repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar(Long bairroId, StatusAlerta status) {
        return alertaRepository.buscar(bairroId, status).stream().map(AlertaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscar(Long id) {
        return AlertaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public AlertaResponse encerrar(Long id) {
        Alerta alerta = buscarEntidade(id);
        if (alerta.getStatus() == StatusAlerta.ENCERRADO) {
            throw new ConflictException("Alerta ja esta encerrado");
        }
        alerta.setStatus(StatusAlerta.ENCERRADO);
        return AlertaResponse.from(alerta);
    }

    private Alerta buscarEntidade(Long id) {
        return alertaRepository.findById(id).orElseThrow(() -> new NotFoundException("Alerta " + id + " nao encontrado"));
    }
}
