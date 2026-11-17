package br.com.fiap.alertacheia.repository;

import br.com.fiap.alertacheia.model.Bairro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BairroRepository extends JpaRepository<Bairro, Long> {
    boolean existsByNomeIgnoreCaseAndCidadeIgnoreCase(String nome, String cidade);
}
