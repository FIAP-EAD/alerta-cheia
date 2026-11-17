package br.com.fiap.alertacheia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de API (integracao) do plano de testes. Rodam com: mvn test
 * Usam o perfil "dev" (H2 em memoria + DevDataSeeder). @Transactional desfaz as alteracoes apos cada teste.
 * Relatorio gerado em target/surefire-reports.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AlertaCheiaApiTest {

    private static final String CHAVE_SENSOR = "sensor-dev-key-123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String token(String email, String senha) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    private String enviarLeitura(String codigo, String valor) throws Exception {
        return mvc.perform(post("/leituras").header("X-Sensor-Key", CHAVE_SENSOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoSensor\":\"%s\",\"valor\":%s}".formatted(codigo, valor)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("CT01 - Cadastro de cidadao valido retorna 201 e nao expoe a senha")
    void ct01_cadastroValido() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Carlos","email":"carlos@email.com","senha":"Senha123",
                                 "telefone":"11955554444","bairroId":1}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("CIDADAO"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    @DisplayName("CT02 - Cadastro com email ja existente retorna 409")
    void ct02_emailDuplicado() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outra Maria","email":"maria@email.com","senha":"Senha123","bairroId":1}"""))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("CT03 - Cadastro com email invalido e senha fraca retorna 400 com os campos")
    void ct03_cadastroInvalido() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"email\":\"nao-e-email\",\"senha\":\"123\",\"bairroId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    @DisplayName("CT04 - Login com senha errada retorna 401")
    void ct04_loginSenhaErrada() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@email.com\",\"senha\":\"errada123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CT05 - Rota protegida sem token retorna 401")
    void ct05_semToken() throws Exception {
        mvc.perform(get("/alertas")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CT06 - Cidadao tentando excluir sensor retorna 403 (controle de acesso)")
    void ct06_cidadaoNaoExcluiSensor() throws Exception {
        mvc.perform(delete("/sensores/1").header("Authorization", token("maria@email.com", "Cidadao@123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CT07 - Leitura acima do limite critico gera alerta CRITICO e muda a situacao do bairro")
    void ct07_leituraCritica() throws Exception {
        JsonNode resp = json.readTree(enviarLeitura("RIO-002", "3.40"));
        org.junit.jupiter.api.Assertions.assertEquals("CRITICO", resp.get("situacao").asText());
        org.junit.jupiter.api.Assertions.assertEquals("CRITICO", resp.get("alertaGerado").get("severidade").asText());

        mvc.perform(get("/bairros/2/situacao").header("Authorization", token("joao@email.com", "Cidadao@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CRITICO"))
                .andExpect(jsonPath("$.alertasAtivos", hasSize(1)));
    }

    @Test
    @DisplayName("CT08 - Leitura normal nao gera alerta")
    void ct08_leituraNormal() throws Exception {
        JsonNode resp = json.readTree(enviarLeitura("CHUVA-003", "5.0"));
        org.junit.jupiter.api.Assertions.assertEquals("NORMAL", resp.get("situacao").asText());
        org.junit.jupiter.api.Assertions.assertTrue(resp.get("alertaGerado").isNull());
    }

    @Test
    @DisplayName("CT09 - Duas leituras criticas seguidas nao duplicam o alerta")
    void ct09_semAlertaDuplicado() throws Exception {
        enviarLeitura("RIO-002", "3.20");
        JsonNode segunda = json.readTree(enviarLeitura("CHUVA-003", "60"));
        org.junit.jupiter.api.Assertions.assertTrue(segunda.get("alertaGerado").isNull());
    }

    @Test
    @DisplayName("CT10 - Leitura sem X-Sensor-Key retorna 401")
    void ct10_leituraSemChave() throws Exception {
        mvc.perform(post("/leituras").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoSensor\":\"RIO-001\",\"valor\":1.0}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CT11 - Abrigo com vagas ocupadas maior que a capacidade retorna 400")
    void ct11_abrigoInvalido() throws Exception {
        mvc.perform(post("/abrigos").header("Authorization", token("admin@alertacheia.com", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Escola X\",\"endereco\":\"Rua Y, 1\",\"capacidade\":10,\"vagasOcupadas\":20,\"bairroId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CT12 - Relato com <script> e salvo escapado (protecao XSS)")
    void ct12_relatoXss() throws Exception {
        mvc.perform(post("/relatos").header("Authorization", token("maria@email.com", "Cidadao@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bairroId\":1,\"descricao\":\"<script>alert(1)</script> agua subindo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao", not(containsString("<script>"))))
                .andExpect(jsonPath("$.descricao", containsString("&lt;script&gt;")));
    }

    @Test
    @DisplayName("CT13 - Recurso inexistente retorna 404")
    void ct13_naoEncontrado() throws Exception {
        mvc.perform(get("/abrigos/9999").header("Authorization", token("maria@email.com", "Cidadao@123")))
                .andExpect(status().isNotFound());
    }
}
