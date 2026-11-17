package br.com.fiap.alertacheia.controller;

import br.com.fiap.alertacheia.dto.AuthDtos.*;
import br.com.fiap.alertacheia.security.UsuarioAutenticado;
import br.com.fiap.alertacheia.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Autenticacao")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um cidadao")
    public UsuarioResponse registrar(@RequestBody @Valid RegisterRequest req) {
        return authService.registrar(req);
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Faz login e devolve o token JWT")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/usuarios/me")
    @Operation(summary = "Dados do usuario logado")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado logado) {
        return authService.buscarPorId(logado.id());
    }
}
