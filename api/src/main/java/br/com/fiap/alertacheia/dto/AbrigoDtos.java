package br.com.fiap.alertacheia.dto;

import br.com.fiap.alertacheia.model.Abrigo;
import jakarta.validation.constraints.*;

public final class AbrigoDtos {
    private AbrigoDtos() {}

    public record AbrigoRequest(
            @NotBlank(message = "Nome e obrigatorio") @Size(max = 100) String nome,
            @NotBlank(message = "Endereco e obrigatorio") @Size(max = 200) String endereco,
            @NotNull(message = "Capacidade e obrigatoria") @Positive(message = "Capacidade deve ser maior que zero")
            @Max(99999) Integer capacidade,
            @NotNull(message = "Vagas ocupadas e obrigatorio") @PositiveOrZero(message = "Vagas ocupadas nao pode ser negativo")
            Integer vagasOcupadas,
            @NotNull(message = "Bairro e obrigatorio") Long bairroId) {}

    public record AbrigoResponse(Long id, String nome, String endereco, int capacidade, int vagasOcupadas,
                                 int vagasLivres, Long bairroId, String bairroNome) {
        public static AbrigoResponse from(Abrigo a) {
            return new AbrigoResponse(a.getId(), a.getNome(), a.getEndereco(), a.getCapacidade(), a.getVagasOcupadas(),
                    a.vagasLivres(), a.getBairro().getId(), a.getBairro().getNome());
        }
    }
}
