package br.com.fiap.alertacheia.controller;

import br.com.fiap.alertacheia.dto.AbrigoDtos.*;
import br.com.fiap.alertacheia.service.AbrigoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/abrigos")
@Tag(name = "Abrigos")
public class AbrigoController {

    private final AbrigoService abrigoService;

    public AbrigoController(AbrigoService abrigoService) {
        this.abrigoService = abrigoService;
    }

    @GetMapping
    public List<AbrigoResponse> listar(@RequestParam(required = false) Long bairroId,
                                       @RequestParam(defaultValue = "false") boolean somenteComVaga) {
        return abrigoService.listar(bairroId, somenteComVaga);
    }

    @GetMapping("/{id}")
    public AbrigoResponse buscar(@PathVariable Long id) {
        return abrigoService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AbrigoResponse criar(@RequestBody @Valid AbrigoRequest req) {
        return abrigoService.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AbrigoResponse atualizar(@PathVariable Long id, @RequestBody @Valid AbrigoRequest req) {
        return abrigoService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void excluir(@PathVariable Long id) {
        abrigoService.excluir(id);
    }

    @PatchMapping("/{id}/ocupacao")
    @PreAuthorize("hasRole('ADMIN')")
    public AbrigoResponse ajustarOcupacao(@PathVariable Long id,
                                          @RequestBody @Valid AjusteOcupacaoRequest req) {
        return abrigoService.ajustarOcupacao(id, req.delta());
    }
}
