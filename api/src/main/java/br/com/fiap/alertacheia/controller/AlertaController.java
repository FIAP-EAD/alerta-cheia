package br.com.fiap.alertacheia.controller;

import br.com.fiap.alertacheia.dto.AlertaDtos.AlertaResponse;
import br.com.fiap.alertacheia.model.StatusAlerta;
import br.com.fiap.alertacheia.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alertas")
@Tag(name = "Alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    @Operation(summary = "Lista alertas, com filtros opcionais por bairro e status")
    public List<AlertaResponse> listar(@RequestParam(required = false) Long bairroId,
                                       @RequestParam(required = false) StatusAlerta status) {
        return alertaService.listar(bairroId, status);
    }

    @GetMapping("/{id}")
    public AlertaResponse buscar(@PathVariable Long id) {
        return alertaService.buscar(id);
    }

    @PutMapping("/{id}/encerrar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Encerra um alerta ativo (Defesa Civil)")
    public AlertaResponse encerrar(@PathVariable Long id) {
        return alertaService.encerrar(id);
    }
}
