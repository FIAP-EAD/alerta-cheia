package br.com.fiap.alertacheia.dto;

import br.com.fiap.alertacheia.model.Leitura;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class LeituraDtos {
    private LeituraDtos() {}

    /** JSON enviado pelos sensores (simulador IoT). */
    public record LeituraRequest(
            @NotBlank(message = "Codigo do sensor e obrigatorio") String codigoSensor,
            @NotNull(message = "Valor e obrigatorio") @PositiveOrZero(message = "Valor nao pode ser negativo")
            @DecimalMax(value = "999999.99") BigDecimal valor) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record LeituraResponse(Long id, String codigoSensor, BigDecimal valor, String unidade, LocalDateTime dataHora,
                                  String situacao, AlertaDtos.AlertaResponse alertaGerado) {
        public static LeituraResponse from(Leitura l, String situacao, AlertaDtos.AlertaResponse alerta) {
            return new LeituraResponse(l.getId(), l.getSensor().getCodigo(), l.getValor(), l.getSensor().unidade(),
                    l.getDataHora(), situacao, alerta);
        }
    }
}
