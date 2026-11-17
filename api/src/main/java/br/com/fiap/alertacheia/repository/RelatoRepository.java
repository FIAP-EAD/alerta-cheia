package br.com.fiap.alertacheia.repository;

import br.com.fiap.alertacheia.model.Relato;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RelatoRepository extends JpaRepository<Relato, Long> {
    List<Relato> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);
    List<Relato> findAllByOrderByCriadoEmDesc();
}
