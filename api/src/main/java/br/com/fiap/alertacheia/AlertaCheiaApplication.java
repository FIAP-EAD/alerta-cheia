package br.com.fiap.alertacheia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Excluimos o usuario em memoria padrao do Spring Security: a autenticacao e feita via JWT.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AlertaCheiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertaCheiaApplication.class, args);
    }
}
