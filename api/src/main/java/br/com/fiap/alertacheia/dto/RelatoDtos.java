package br.com.fiap.alertacheia.dto;

import br.com.fiap.alertacheia.model.Relato;
import br.com.fiap.alertacheia.model.StatusRelato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class RelatoDtos {
    private RelatoDtos() {}

    public record RelatoRequest(
            @NotNull(message = "Bairro e obrigatorio") Long bairroId,
            @NotBlank(message = "Descricao e obrigatoria") @Size(min = 5, max = 500, message = "Descricao deve ter de 5 a 500 caracteres")
            String descricao) {}

    public record RelatoStatusRequest(@NotNull(message = "Status e obrigatorio") StatusRelato status) {}

    public record RelatoResponse(Long id, Long usuarioId, String usuarioNome, Long bairroId, String bairroNome,
                                 String descricao, StatusRelato status, LocalDateTime criadoEm) {
        public static RelatoResponse from(Relato r) {
            return new RelatoResponse(r.getId(), r.getUsuario().getId(), r.getUsuario().getNome(), r.getBairro().getId(),
                    r.getBairro().getNome(), r.getDescricao(), r.getStatus(), r.getCriadoEm());
        }
    }
}
