package br.com.fiap.alertacheia.dto;

import br.com.fiap.alertacheia.model.Bairro;
import br.com.fiap.alertacheia.model.NivelRisco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class BairroDtos {
    private BairroDtos() {}

    public record BairroRequest(
            @NotBlank(message = "Nome e obrigatorio") @Size(max = 100) String nome,
            @NotBlank(message = "Cidade e obrigatoria") @Size(max = 100) String cidade,
            @NotNull(message = "Nivel de risco e obrigatorio") NivelRisco nivelRisco) {}

    public record BairroResponse(Long id, String nome, String cidade, NivelRisco nivelRisco) {
        public static BairroResponse from(Bairro b) {
            return new BairroResponse(b.getId(), b.getNome(), b.getCidade(), b.getNivelRisco());
        }
    }

    /** Resumo usado pela tela "Meu bairro" do app: NORMAL, ATENCAO ou CRITICO. */
    public record SituacaoBairroResponse(BairroResponse bairro, String situacao,
                                         List<AlertaDtos.AlertaResponse> alertasAtivos) {}
}
