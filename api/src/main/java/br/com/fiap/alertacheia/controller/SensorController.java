package br.com.fiap.alertacheia.controller;

import br.com.fiap.alertacheia.dto.SensorDtos.*;
import br.com.fiap.alertacheia.service.SensorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sensores")
@Tag(name = "Sensores (ADMIN)")
@PreAuthorize("hasRole('ADMIN')") // todas as rotas: somente Defesa Civil
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    public List<SensorResponse> listar(@RequestParam(required = false) Long bairroId) {
        return sensorService.listar(bairroId);
    }

    @GetMapping("/{id}")
    public SensorResponse buscar(@PathVariable Long id) {
        return sensorService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorResponse criar(@RequestBody @Valid SensorRequest req) {
        return sensorService.criar(req);
    }

    @PutMapping("/{id}")
    public SensorResponse atualizar(@PathVariable Long id, @RequestBody @Valid SensorRequest req) {
        return sensorService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        sensorService.excluir(id);
    }
}
