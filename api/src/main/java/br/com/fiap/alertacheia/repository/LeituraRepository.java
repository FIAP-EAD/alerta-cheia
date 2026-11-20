package br.com.fiap.alertacheia.repository;

import br.com.fiap.alertacheia.model.Leitura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeituraRepository extends JpaRepository<Leitura, Long> {
    List<Leitura> findTop50BySensorIdOrderByDataHoraDesc(Long sensorId);
    List<Leitura> findTop3BySensorIdOrderByDataHoraDescIdDesc(Long sensorId);
}
