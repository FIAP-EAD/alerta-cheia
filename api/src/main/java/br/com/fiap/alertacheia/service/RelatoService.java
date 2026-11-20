package br.com.fiap.alertacheia.service;

import br.com.fiap.alertacheia.dto.RelatoDtos.*;
import br.com.fiap.alertacheia.exception.BusinessException;
import br.com.fiap.alertacheia.exception.ForbiddenException;
import br.com.fiap.alertacheia.exception.NotFoundException;
import br.com.fiap.alertacheia.model.Relato;
import br.com.fiap.alertacheia.model.Usuario;
import br.com.fiap.alertacheia.repository.RelatoRepository;
import br.com.fiap.alertacheia.repository.UsuarioRepository;
import br.com.fiap.alertacheia.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class RelatoService {

    private final RelatoRepository relatoRepository;
    private final UsuarioRepository usuarioRepository;
    private final BairroService bairroService;

    public RelatoService(RelatoRepository relatoRepository, UsuarioRepository usuarioRepository, BairroService bairroService) {
        this.relatoRepository = relatoRepository;
        this.usuarioRepository = usuarioRepository;
        this.bairroService = bairroService;
    }

    @Transactional
    public RelatoResponse criar(UsuarioAutenticado logado, RelatoRequest req) {
        Usuario usuario = usuarioRepository.findById(logado.id())
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
        // Protecao contra XSS: texto livre e salvo com HTML escapado (<script> vira &lt;script&gt;)
        String descricao = HtmlUtils.htmlEscape(req.descricao().trim(), "UTF-8");
        if (descricao.length() > 1000) {
            throw new BusinessException("Descricao muito longa");
        }
        Relato relato = new Relato(usuario, bairroService.buscarEntidade(req.bairroId()), descricao);
        return RelatoResponse.from(relatoRepository.save(relato));
    }

    /** ADMIN ve todos os relatos; CIDADAO ve apenas os proprios. */
    @Transactional(readOnly = true)
    public List<RelatoResponse> listar(UsuarioAutenticado logado) {
        List<Relato> relatos = logado.isAdmin() ? relatoRepository.findAllByOrderByCriadoEmDesc()
                : relatoRepository.findByUsuarioIdOrderByCriadoEmDesc(logado.id());
        return relatos.stream().map(RelatoResponse::from).toList();
    }

    @Transactional
    public RelatoResponse atualizarStatus(Long id, RelatoStatusRequest req) {
        Relato relato = buscarEntidade(id);
        relato.setStatus(req.status());
        return RelatoResponse.from(relato);
    }

    @Transactional
    public void excluir(UsuarioAutenticado logado, Long id) {
        Relato relato = buscarEntidade(id);
        if (!logado.isAdmin() && !relato.getUsuario().getId().equals(logado.id())) {
            throw new ForbiddenException("Relato pertence a outro usuario");
        }
        relatoRepository.delete(relato);
    }

    private Relato buscarEntidade(Long id) {
        return relatoRepository.findById(id).orElseThrow(() -> new NotFoundException("Relato " + id + " nao encontrado"));
    }
}
