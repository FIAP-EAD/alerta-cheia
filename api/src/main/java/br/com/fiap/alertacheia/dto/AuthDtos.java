package br.com.fiap.alertacheia.dto;

import br.com.fiap.alertacheia.model.Perfil;
import br.com.fiap.alertacheia.model.Usuario;
import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "Nome e obrigatorio") @Size(max = 100) String nome,
            @NotBlank(message = "Email e obrigatorio") @Email(message = "Email invalido") @Size(max = 150) String email,
            @NotBlank(message = "Senha e obrigatoria")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                     message = "Senha deve ter de 8 a 72 caracteres, com letras e numeros") String senha,
            @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve ter 10 ou 11 digitos (somente numeros)") String telefone,
            @NotNull(message = "Bairro e obrigatorio") Long bairroId) {}

    public record LoginRequest(
            @NotBlank(message = "Email e obrigatorio") @Email(message = "Email invalido") String email,
            @NotBlank(message = "Senha e obrigatoria") String senha) {}

    public record TokenResponse(String token, String tipo, long expiraEmSegundos, UsuarioResponse usuario) {}

    public record UsuarioResponse(Long id, String nome, String email, Perfil perfil, String telefone,
                                  Long bairroId, String bairroNome) {
        public static UsuarioResponse from(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.getTelefone(),
                    u.getBairro().getId(), u.getBairro().getNome());
        }
    }
}
