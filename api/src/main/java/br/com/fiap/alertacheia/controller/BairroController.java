package br.com.fiap.alertacheia.controller;

import br.com.fiap.alertacheia.dto.BairroDtos.*;
import br.com.fiap.alertacheia.service.BairroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bairros")
@Tag(name = "Bairros")
public class BairroController {

    private final BairroService bairroService;

    public BairroController(BairroService bairroService) {
        this.bairroService = bairroService;
    }

    @GetMapping
    @Operation(summary = "Lista bairros (publico, usado na tela de cadastro)")
    public List<BairroResponse> listar() {
        return bairroService.listar();
    }

    @GetMapping("/{id}")
    public BairroResponse buscar(@PathVariable Long id) {
        return bairroService.buscar(id);
    }

    @GetMapping("/{id}/situacao")
    @Operation(summary = "Situacao atual do bairro (NORMAL/ATENCAO/CRITICO) e alertas ativos")
    public SituacaoBairroResponse situacao(@PathVariable Long id) {
        return bairroService.situacao(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BairroResponse criar(@RequestBody @Valid BairroRequest req) {
        return bairroService.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BairroResponse atualizar(@PathVariable Long id, @RequestBody @Valid BairroRequest req) {
        return bairroService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void excluir(@PathVariable Long id) {
        bairroService.excluir(id);
    }
}
