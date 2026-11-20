package br.com.fiap.alertacheia.service;

import br.com.fiap.alertacheia.dto.AuthDtos.*;
import br.com.fiap.alertacheia.exception.ConflictException;
import br.com.fiap.alertacheia.exception.NotFoundException;
import br.com.fiap.alertacheia.exception.UnauthorizedException;
import br.com.fiap.alertacheia.model.Bairro;
import br.com.fiap.alertacheia.model.Perfil;
import br.com.fiap.alertacheia.model.Usuario;
import br.com.fiap.alertacheia.repository.UsuarioRepository;
import br.com.fiap.alertacheia.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BairroService bairroService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashFixo;

    public AuthService(UsuarioRepository usuarioRepository, BairroService bairroService,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.bairroService = bairroService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFixo = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /** Todo cadastro publico e de CIDADAO. Admins (Defesa Civil) vem da carga inicial do banco. */
    @Transactional
    public UsuarioResponse registrar(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email ja cadastrado");
        }
        Bairro bairro = bairroService.buscarEntidade(req.bairroId());
        String hash = passwordEncoder.encode(req.senha());
        Usuario usuario = new Usuario(req.nome().trim(), email, hash, Perfil.CIDADAO, req.telefone(), bairro);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        // Mesma mensagem para email inexistente e senha errada: nao revela quais emails existem.
        // Quando o email nao existe, rodamos BCrypt com hashFixo para nivelar o tempo de resposta
        // e evitar que um atacante enumere emails medindo a diferenca de latencia.
        java.util.Optional<Usuario> opt = usuarioRepository.findByEmailIgnoreCase(req.email().trim());
        if (opt.isEmpty()) {
            passwordEncoder.matches(req.senha(), hashFixo);
            throw new UnauthorizedException("Email ou senha invalidos");
        }
        Usuario usuario = opt.filter(u -> passwordEncoder.matches(req.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new UnauthorizedException("Email ou senha invalidos"));
        return new TokenResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.getExpiracaoSegundos(),
                UsuarioResponse.from(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return usuarioRepository.findById(id).map(UsuarioResponse::from)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
    }
}
