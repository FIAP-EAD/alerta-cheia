package br.com.fiap.alertacheia.config;

import br.com.fiap.alertacheia.model.*;
import br.com.fiap.alertacheia.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Somente no perfil "dev" (banco H2 em memoria): cria os mesmos dados basicos do script Oracle,
 * para rodar a API e os testes sem depender do Oracle. Em producao os dados vem do alerta_cheia.sql.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final BairroRepository bairros;
    private final UsuarioRepository usuarios;
    private final SensorRepository sensores;
    private final AbrigoRepository abrigos;
    private final PasswordEncoder encoder;

    public DevDataSeeder(BairroRepository bairros, UsuarioRepository usuarios, SensorRepository sensores,
                         AbrigoRepository abrigos, PasswordEncoder encoder) {
        this.bairros = bairros;
        this.usuarios = usuarios;
        this.sensores = sensores;
        this.abrigos = abrigos;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (bairros.count() > 0) return;

        Bairro pantanal = bairros.save(new Bairro("Jardim Pantanal", "Sao Paulo", NivelRisco.ALTO));
        Bairro romano = bairros.save(new Bairro("Jardim Romano", "Sao Paulo", NivelRisco.ALTO));
        Bairro itaim = bairros.save(new Bairro("Vila Itaim", "Sao Paulo", NivelRisco.MEDIO));
        Bairro mariana = bairros.save(new Bairro("Vila Mariana", "Sao Paulo", NivelRisco.BAIXO));

        usuarios.save(new Usuario("Defesa Civil", "admin@alertacheia.com", encoder.encode("Admin@123"), Perfil.ADMIN, "1199990000", mariana));
        usuarios.save(new Usuario("Maria Souza", "maria@email.com", encoder.encode("Cidadao@123"), Perfil.CIDADAO, "11988881111", pantanal));
        usuarios.save(new Usuario("Joao Lima", "joao@email.com", encoder.encode("Cidadao@123"), Perfil.CIDADAO, "11977772222", romano));
        usuarios.save(new Usuario("Ana Costa", "ana@email.com", encoder.encode("Cidadao@123"), Perfil.CIDADAO, null, itaim));

        sensores.save(new Sensor("RIO-001", TipoSensor.NIVEL_RIO, pantanal, new BigDecimal("2.00"), new BigDecimal("3.00")));
        sensores.save(new Sensor("CHUVA-001", TipoSensor.PLUVIOMETRO, pantanal, new BigDecimal("30.00"), new BigDecimal("50.00")));
        sensores.save(new Sensor("RIO-002", TipoSensor.NIVEL_RIO, romano, new BigDecimal("2.00"), new BigDecimal("3.00")));
        sensores.save(new Sensor("CHUVA-002", TipoSensor.PLUVIOMETRO, itaim, new BigDecimal("30.00"), new BigDecimal("50.00")));
        sensores.save(new Sensor("CHUVA-003", TipoSensor.PLUVIOMETRO, romano, new BigDecimal("30.00"), new BigDecimal("50.00")));

        abrigos.save(new Abrigo("EMEF Jardim Pantanal", "Rua A, 100", 120, 45, pantanal));
        abrigos.save(new Abrigo("Igreja Sao Jose", "Rua B, 250", 60, 60, pantanal));
        abrigos.save(new Abrigo("CEU Jardim Romano", "Av. C, 1200", 200, 20, romano));
        abrigos.save(new Abrigo("Ginasio Vila Itaim", "Rua D, 30", 80, 0, itaim));
    }
}
