package br.com.fiap.alertacheia;

import br.com.fiap.alertacheia.model.*;
import br.com.fiap.alertacheia.service.LeituraService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Teste de unidade da regra de classificacao (sem Spring, sem banco). */
class LeituraServiceTest {

    private final Sensor rio = new Sensor("RIO-T", TipoSensor.NIVEL_RIO, new Bairro("B", "C", NivelRisco.ALTO),
            new BigDecimal("2.00"), new BigDecimal("3.00"));

    @Test
    @DisplayName("Abaixo do limite de alerta: sem severidade")
    void normal() {
        assertNull(LeituraService.classificar(rio, new BigDecimal("1.99")));
    }

    @Test
    @DisplayName("Exatamente no limite de alerta: ATENCAO")
    void atencao() {
        assertEquals(Severidade.ATENCAO, LeituraService.classificar(rio, new BigDecimal("2.00")));
    }

    @Test
    @DisplayName("No limite critico ou acima: CRITICO")
    void critico() {
        assertEquals(Severidade.CRITICO, LeituraService.classificar(rio, new BigDecimal("3.00")));
        assertEquals(Severidade.CRITICO, LeituraService.classificar(rio, new BigDecimal("5.50")));
    }
}
