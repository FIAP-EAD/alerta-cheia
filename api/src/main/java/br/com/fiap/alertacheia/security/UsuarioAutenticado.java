package br.com.fiap.alertacheia.security;

import br.com.fiap.alertacheia.model.Perfil;

/** Dados do usuario logado, extraidos do token JWT. Use com @AuthenticationPrincipal. */
public record UsuarioAutenticado(Long id, String email, Perfil perfil) {
    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }
}
