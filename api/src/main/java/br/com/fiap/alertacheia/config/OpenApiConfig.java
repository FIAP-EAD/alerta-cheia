package br.com.fiap.alertacheia.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Swagger UI em /swagger-ui.html. Clique em "Authorize" e cole o token do /auth/login. */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Alerta Cheia API", version = "1.0",
                description = "Alertas de enchente por bairro - Global Solution 2026/2 (CitizenTech)"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
