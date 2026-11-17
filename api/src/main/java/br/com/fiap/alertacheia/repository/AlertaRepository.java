package br.com.fiap.alertacheia.repository;

import br.com.fiap.alertacheia.model.Alerta;
import br.com.fiap.alertacheia.model.Severidade;
import br.com.fiap.alertacheia.model.StatusAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    // Consulta com filtros opcionais: parametro nulo = sem filtro
    @Query("""
           SELECT a FROM Alerta a JOIN FETCH a.bairro
            WHERE (:bairroId IS NULL OR a.bairro.id = :bairroId)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.criadoEm DESC
           """)
    List<Alerta> buscar(@Param("bairroId") Long bairroId, @Param("status") StatusAlerta status);

    List<Alerta> findByBairroIdAndStatus(Long bairroId, StatusAlerta status);

    boolean existsByBairroIdAndStatusAndSeveridadeIn(Long bairroId, StatusAlerta status, Collection<Severidade> severidades);
}
