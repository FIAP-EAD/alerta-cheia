package br.com.fiap.alertacheia;

import br.com.fiap.alertacheia.dto.LeituraDtos.LeituraRequest;
import br.com.fiap.alertacheia.model.Severidade;
import br.com.fiap.alertacheia.model.StatusAlerta;
import br.com.fiap.alertacheia.repository.AlertaRepository;
import br.com.fiap.alertacheia.repository.SensorRepository;
import br.com.fiap.alertacheia.service.LeituraService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifica que leituras criticas concorrentes do mesmo bairro nao geram alertas duplicados.
 * SEM @Transactional para que cada thread use sua propria transacao.
 */
@SpringBootTest
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LeituraConcorrenciaTest {

    @Autowired LeituraService leituraService;
    @Autowired AlertaRepository alertaRepository;
    @Autowired SensorRepository sensorRepository;
    @Autowired PlatformTransactionManager txManager;
    @Value("${app.sensor.api-key}") String chave;

    @Test
    @DisplayName("CT - 8 threads criticas do RIO-001 geram exatamente 1 alerta CRITICO ATIVO")
    void concorrencia_semAlertaDuplicado() throws InterruptedException {
        int threads = 8;
        CountDownLatch pronto = new CountDownLatch(1);
        CountDownLatch fim = new CountDownLatch(threads);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    pronto.await();
                    leituraService.registrar(chave, new LeituraRequest("RIO-001", new BigDecimal("3.50")));
                } catch (Exception ignored) {
                } finally {
                    fim.countDown();
                }
            });
        }

        pronto.countDown();
        fim.await();
        pool.shutdown();

        // Asserção dentro de uma transação para evitar LazyInitializationException
        new TransactionTemplate(txManager).execute(status -> {
            Long bairroId = sensorRepository.findByCodigo("RIO-001")
                    .map(s -> s.getBairro().getId())
                    .orElseThrow();
            long ativos = alertaRepository.findByBairroIdAndStatus(bairroId, StatusAlerta.ATIVO)
                    .stream()
                    .filter(a -> a.getSeveridade() == Severidade.CRITICO)
                    .count();
            assertEquals(1, ativos, "Deve existir exatamente 1 alerta CRITICO ATIVO no bairro do RIO-001");
            return null;
        });
    }
}
