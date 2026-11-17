package br.com.fiap.alertacheia.dto;

import br.com.fiap.alertacheia.model.Alerta;
import br.com.fiap.alertacheia.model.Severidade;
import br.com.fiap.alertacheia.model.StatusAlerta;
import java.time.LocalDateTime;

public final class AlertaDtos {
    private AlertaDtos() {}

    public record AlertaResponse(Long id, Long bairroId, String bairroNome, Severidade severidade, String mensagem,
                                 StatusAlerta status, LocalDateTime criadoEm) {
        public static AlertaResponse from(Alerta a) {
            return new AlertaResponse(a.getId(), a.getBairro().getId(), a.getBairro().getNome(), a.getSeveridade(),
                    a.getMensagem(), a.getStatus(), a.getCriadoEm());
        }
    }
}
