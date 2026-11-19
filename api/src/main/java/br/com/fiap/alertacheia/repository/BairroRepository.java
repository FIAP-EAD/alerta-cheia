package br.com.fiap.alertacheia.repository;

import br.com.fiap.alertacheia.model.Bairro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BairroRepository extends JpaRepository<Bairro, Long> {
    boolean existsByNomeIgnoreCaseAndCidadeIgnoreCase(String nome, String cidade);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bairro b where b.id = :id")
    Optional<Bairro> travarPorId(@Param("id") Long id);
}
