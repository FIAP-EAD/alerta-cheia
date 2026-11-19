package br.com.fiap.alertacheia.service;

import br.com.fiap.alertacheia.dto.AbrigoDtos.*;
import br.com.fiap.alertacheia.exception.BusinessException;
import br.com.fiap.alertacheia.exception.ConflictException;
import br.com.fiap.alertacheia.exception.NotFoundException;
import br.com.fiap.alertacheia.model.Abrigo;
import br.com.fiap.alertacheia.repository.AbrigoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AbrigoService {

    private final AbrigoRepository abrigoRepository;
    private final BairroService bairroService;

    public AbrigoService(AbrigoRepository abrigoRepository, BairroService bairroService) {
        this.abrigoRepository = abrigoRepository;
        this.bairroService = bairroService;
    }

    @Transactional(readOnly = true)
    public List<AbrigoResponse> listar(Long bairroId, boolean somenteComVaga) {
        return abrigoRepository.buscar(bairroId, somenteComVaga).stream().map(AbrigoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AbrigoResponse buscar(Long id) {
        return AbrigoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public AbrigoResponse criar(AbrigoRequest req) {
        validarVagas(req);
        Abrigo abrigo = new Abrigo(req.nome().trim(), req.endereco().trim(), req.capacidade(), req.vagasOcupadas(),
                bairroService.buscarEntidade(req.bairroId()));
        return AbrigoResponse.from(abrigoRepository.save(abrigo));
    }

    @Transactional
    public AbrigoResponse atualizar(Long id, AbrigoRequest req) {
        validarVagas(req);
        Abrigo abrigo = buscarEntidade(id);
        abrigo.setNome(req.nome().trim());
        abrigo.setEndereco(req.endereco().trim());
        abrigo.setCapacidade(req.capacidade());
        abrigo.setVagasOcupadas(req.vagasOcupadas());
        abrigo.setBairro(bairroService.buscarEntidade(req.bairroId()));
        return AbrigoResponse.from(abrigo);
    }

    @Transactional
    public void excluir(Long id) {
        abrigoRepository.delete(buscarEntidade(id));
    }

    @Transactional
    public AbrigoResponse ajustarOcupacao(Long id, int delta) {
        int linhasAfetadas = abrigoRepository.ajustarOcupacao(id, delta);
        if (linhasAfetadas == 0) {
            if (!abrigoRepository.existsById(id)) {
                throw new NotFoundException("Abrigo " + id + " nao encontrado");
            }
            throw new ConflictException("Ocupacao fora dos limites do abrigo");
        }
        return AbrigoResponse.from(buscarEntidade(id));
    }

    private Abrigo buscarEntidade(Long id) {
        return abrigoRepository.findById(id).orElseThrow(() -> new NotFoundException("Abrigo " + id + " nao encontrado"));
    }

    private void validarVagas(AbrigoRequest req) {
        if (req.vagasOcupadas() > req.capacidade()) {
            throw new BusinessException("Vagas ocupadas nao pode ser maior que a capacidade");
        }
    }
}
