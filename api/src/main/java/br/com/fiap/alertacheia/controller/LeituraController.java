package br.com.fiap.alertacheia.controller;

import br.com.fiap.alertacheia.dto.LeituraDtos.*;
import br.com.fiap.alertacheia.service.LeituraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leituras")
@Tag(name = "Leituras (IoT)")
public class LeituraController {

    private final LeituraService leituraService;

    public LeituraController(LeituraService leituraService) {
        this.leituraService = leituraService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements // nao usa JWT: o sensor se autentica pelo header X-Sensor-Key
    @Operation(summary = "Recebe a leitura de um sensor e gera alerta se passar do limite")
    public LeituraResponse registrar(@RequestHeader(value = "X-Sensor-Key", required = false) String chave,
                                     @RequestBody @Valid LeituraRequest req) {
        return leituraService.registrar(chave, req);
    }

    @GetMapping
    @Operation(summary = "Ultimas 50 leituras de um sensor")
    public List<LeituraResponse> ultimas(@RequestParam Long sensorId) {
        return leituraService.ultimas(sensorId);
    }
}
