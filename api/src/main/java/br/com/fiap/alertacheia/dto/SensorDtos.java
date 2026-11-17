package br.com.fiap.alertacheia.dto;

import br.com.fiap.alertacheia.model.Sensor;
import br.com.fiap.alertacheia.model.TipoSensor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class SensorDtos {
    private SensorDtos() {}

    public record SensorRequest(
            @NotBlank(message = "Codigo e obrigatorio")
            @Pattern(regexp = "^[A-Z0-9-]{3,30}$", message = "Codigo deve ter de 3 a 30 caracteres: letras maiusculas, numeros e hifen")
            String codigo,
            @NotNull(message = "Tipo e obrigatorio") TipoSensor tipo,
            @NotNull(message = "Bairro e obrigatorio") Long bairroId,
            @NotNull @PositiveOrZero BigDecimal limiteAlerta,
            @NotNull @Positive BigDecimal limiteCritico,
            Boolean ativo) {}

    public record SensorResponse(Long id, String codigo, TipoSensor tipo, String unidade, Long bairroId, String bairroNome,
                                 BigDecimal limiteAlerta, BigDecimal limiteCritico, boolean ativo) {
        public static SensorResponse from(Sensor s) {
            return new SensorResponse(s.getId(), s.getCodigo(), s.getTipo(), s.unidade(), s.getBairro().getId(),
                    s.getBairro().getNome(), s.getLimiteAlerta(), s.getLimiteCritico(), s.isAtivo());
        }
    }
}
