package br.com.fiap.alertacheia.repository;

import br.com.fiap.alertacheia.model.Abrigo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AbrigoRepository extends JpaRepository<Abrigo, Long> {

    @Query("""
           SELECT ab FROM Abrigo ab JOIN FETCH ab.bairro
            WHERE (:bairroId IS NULL OR ab.bairro.id = :bairroId)
              AND (:somenteComVaga = false OR ab.vagasOcupadas < ab.capacidade)
            ORDER BY (ab.capacidade - ab.vagasOcupadas) DESC
           """)
    List<Abrigo> buscar(@Param("bairroId") Long bairroId, @Param("somenteComVaga") boolean somenteComVaga);
}
