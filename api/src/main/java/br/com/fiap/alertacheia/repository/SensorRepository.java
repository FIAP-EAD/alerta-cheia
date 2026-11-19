package br.com.fiap.alertacheia.repository;

import br.com.fiap.alertacheia.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Sensor> findByBairroId(Long bairroId);
    List<Sensor> findByBairroIdAndAtivoTrue(Long bairroId);
}
