package br.com.fiap.alertacheia.controller;

import br.com.fiap.alertacheia.dto.RelatoDtos.*;
import br.com.fiap.alertacheia.security.UsuarioAutenticado;
import br.com.fiap.alertacheia.service.RelatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/relatos")
@Tag(name = "Relatos dos cidadaos")
public class RelatoController {

    private final RelatoService relatoService;

    public RelatoController(RelatoService relatoService) {
        this.relatoService = relatoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cidadao relata o que esta vendo no bairro")
    public RelatoResponse criar(@AuthenticationPrincipal UsuarioAutenticado logado, @RequestBody @Valid RelatoRequest req) {
        return relatoService.criar(logado, req);
    }

    @GetMapping
    @Operation(summary = "ADMIN ve todos; CIDADAO ve os proprios")
    public List<RelatoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado logado) {
        return relatoService.listar(logado);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Defesa Civil marca o relato como VERIFICADO ou DESCARTADO")
    public RelatoResponse atualizarStatus(@PathVariable Long id, @RequestBody @Valid RelatoStatusRequest req) {
        return relatoService.atualizarStatus(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui relato (dono ou ADMIN)")
    public void excluir(@AuthenticationPrincipal UsuarioAutenticado logado, @PathVariable Long id) {
        relatoService.excluir(logado, id);
    }
}
