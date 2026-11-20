# 🌊 Alerta Cheia

> CitizenTech de alerta de enchentes por bairro. Global Solution 2026/2, FIAP, 3ESOA.

Sensores medem o nível do córrego e a intensidade da chuva e enviam as leituras para a API.
Quando um limite é ultrapassado, a API cria um alerta. O morador vê esse alerta no app, junto com
os abrigos com vaga, e pode enviar relatos do que está acontecendo no bairro.

**ODS:** 11 (cidades sustentáveis), 13 (ação climática) e 10 (redução das desigualdades).

🎥 **Vídeo pitch:** https://youtu.be/lmJi1HKYacs

## Integrantes

| Nome | RM |
|---|---|
| Lucas | RM554589 |
| Yuri Lopes | RM555522 |
| Thiago Oliveira | RM555485 |

---

## Estrutura

```
alerta-cheia/
├── database/alerta_cheia.sql   # DDL + carga inicial + 7 consultas (Oracle)
├── api/                        # API REST Java 17 + Spring Boot 3.5
├── iot/simulador.py            # simulador dos 2 sensores
├── web/                        # app web mobile-first em React (Vite)
├── docs/entregaveis.pdf        # PDF com os entregáveis de cada parte
├── docs/openapi.json           # documentação OpenAPI exportada (importe no Postman)
└── integrantes.txt             # grupo, RMs e link do vídeo
```

## Pré-requisitos

- Java 17 ou superior e Maven 3.9 ou superior
- Oracle (servidor da FIAP), ou nenhum banco se usar o perfil `dev` com H2 em memória
- Python 3.10 ou superior, para o simulador (usa só a biblioteca padrão)

## 1. Banco de dados (Oracle)

1. Abra `database/alerta_cheia.sql` no SQL Developer, conectado ao seu usuário Oracle.
2. Execute o script inteiro com **F5 (Executar Script)**. Ele apaga e recria as tabelas, faz a carga inicial e roda as consultas.

**Entidades:** `TB_BAIRRO`, `TB_USUARIO`, `TB_SENSOR`, `TB_LEITURA`, `TB_ALERTA`, `TB_ABRIGO`, `TB_RELATO`.

## 2. API

**Opção A: Oracle**

```bash
cd api
export DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
export DB_USER="rmXXXXX"
export DB_PASSWORD="sua_senha"
mvn spring-boot:run
```

**Opção B: H2 em memória (sem Oracle, já vem com dados de exemplo)**

```bash
cd api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- **API:** http://localhost:8080
- **Swagger:** http://localhost:8080/swagger-ui.html. Faça o login em `/auth/login`, clique em **Authorize** e cole o token.

### Usuários de teste

Estão na carga inicial e também no perfil `dev`.

| Perfil | Email | Senha |
|---|---|---|
| ADMIN (Defesa Civil) | admin@alertacheia.com | Admin@123 |
| CIDADAO (Jardim Pantanal) | maria@email.com | Cidadao@123 |
| CIDADAO (Jardim Romano) | joao@email.com | Cidadao@123 |

### Endpoints

| Método | Rota | Acesso |
|---|---|---|
| POST | `/auth/register` | público |
| POST | `/auth/login` | público |
| GET | `/usuarios/me` | logado |
| GET | `/bairros` | público |
| GET | `/bairros/{id}`, `/bairros/{id}/situacao` | logado |
| POST, PUT, DELETE | `/bairros` | ADMIN |
| GET, POST, PUT, DELETE | `/sensores` | ADMIN |
| POST | `/leituras` | sensor (header `X-Sensor-Key`) |
| GET | `/leituras?sensorId=` | logado |
| GET | `/alertas?bairroId=&status=` | logado |
| GET | `/alertas/{id}` | logado |
| PUT | `/alertas/{id}/encerrar` | ADMIN |
| GET | `/abrigos?bairroId=&somenteComVaga=` | logado |
| POST, PUT, DELETE | `/abrigos` | ADMIN |
| PATCH | `/abrigos/{id}/ocupacao` | ADMIN |
| POST | `/relatos` | logado |
| GET | `/relatos` | logado (cidadão vê só os próprios relatos) |
| PUT | `/relatos/{id}/status` | ADMIN |
| DELETE | `/relatos/{id}` | dono ou ADMIN |

**Camadas:** `controller` → `service` → `repository`, mais `dto`, `model`, `security` e `exception`.

**Status HTTP:**

| Código | Quando |
|---|---|
| 200 / 201 / 204 | sucesso |
| 400 | validação ou regra de negócio |
| 401 | sem token ou credenciais inválidas |
| 403 | perfil sem permissão |
| 404 | recurso não encontrado |
| 409 | duplicado ou com vínculos |

## 3. Simulação de IoT

Com a API rodando:

```bash
cd iot
python simulador.py --cenario enchente --intervalo 3
```

**Os sensores** (bairro Jardim Romano):

| Sensor | O que mede | Atenção | Crítico |
|---|---|---|---|
| `RIO-002` (NIVEL_RIO) | nível do córrego, em metros | ≥ 2,0 m | ≥ 3,0 m |
| `CHUVA-003` (PLUVIOMETRO) | intensidade da chuva, em mm/h | ≥ 30 | ≥ 50 |

**Como os dados influenciam o sistema:** cada leitura é enviada por `POST /leituras`. O `LeituraService` compara o valor com os limites do sensor e cria um alerta `ATENCAO` ou `CRITICO` para o bairro.
- Não cria alerta duplicado enquanto já existe um alerta igual ou mais grave ativo (garantido por lock pessimista no bairro).
- Um alerta CRITICO encerra os alertas de ATENÇÃO do bairro.
- **Encerramento automático (histerese):** quando a leitura é NORMAL, o sistema verifica se todos os sensores ativos do bairro têm as últimas 3 leituras abaixo de 90 % do limite de alerta e estáveis ou descendo. Se sim, todos os alertas ativos do bairro são encerrados automaticamente.

O app consulta `GET /bairros/{id}/situacao` e muda a cor do card para NORMAL, ATENÇÃO ou CRÍTICO.

**Cenários disponíveis:**
- `normal`: valores baixos.
- `chuva`: chega até ATENÇÃO.
- `enchente`: sobe de forma previsível até CRÍTICO. É o ideal para o vídeo.
- `recuo`: o rio desce de 3,2 m em passos de 0,4 m até 0,9 m (chuva cai de 40 para 5 mm/h). Demonstra o encerramento automático ao vivo.

## 4. Frontend web mobile-first (React)

Com a API rodando:

```bash
cd web
npm install
npm run dev
```

- **Endereço:** http://localhost:5173. No navegador, use as ferramentas de desenvolvedor no modo celular, ou abra no próprio celular pelo IP da máquina na mesma rede.
- **Outro endereço de API:** copie `.env.example` para `.env` e ajuste `VITE_API_URL`.

### Telas

| Tela | O que faz |
|---|---|
| Login / Cadastro | Validação dos campos com mensagens de erro, e a lista de bairros vem da API. |
| Início | Card verde, amarelo ou vermelho com a situação do bairro, atualizado a cada 10 s, e os alertas ativos. No celular, o aparelho vibra quando a situação piora. |
| Alertas e Detalhe do alerta | Filtros por ativos e por bairro. O detalhe mostra os abrigos com vaga, e o admin pode encerrar o alerta. |
| Abrigos | Ocupação em barra e link "Como chegar". O admin ajusta a ocupação com os botões + e −. |
| Relatos e Novo relato | O cidadão envia e exclui os próprios relatos. |
| Gestão (só ADMIN) | Encerrar alertas, verificar ou descartar relatos e ativar ou desativar sensores. |

### Integração e usabilidade

- **Falhas de conexão:**
  - Todas as chamadas têm timeout de 8 s e mostram mensagens de erro.
  - Sem conexão, o app mostra os últimos dados salvos (situação e abrigos), com o aviso "Sem conexão".
  - Se a sessão expirar (401), o usuário volta para a tela de login.
- **Usabilidade:**
  - Layout mobile-first com fonte de 17 px e alvos de toque de 44 a 48 px.
  - Modo claro e escuro automáticos.
  - Feedback (toast) em toda ação, com `aria-live` e `role="alert"` para leitores de tela.

## 5. Testes automatizados

```bash
cd api
mvn test
```

São **24 testes** automatizados: 20 de API com MockMvc + H2 (`AlertaCheiaApiTest`: CT01–CT13, CT19–CT21 e CT23–CT26), 1 de concorrência (`LeituraConcorrenciaTest`: CT22, sem `@Transactional`) e 3 de unidade da regra de classificação (`LeituraServiceTest`). O relatório fica em `api/target/surefire-reports/`.

## 6. Segurança

- **Senhas:** guardadas com hash **BCrypt**, nunca em texto puro. As respostas usam DTOs e nunca devolvem o hash.
- **JWT:** token com o perfil do usuário. O controle de acesso por perfil (ADMIN ou CIDADAO) é feito com `@PreAuthorize`.
- **Validação de entrada:** Bean Validation (`@Valid`, `@Email`, `@Pattern`, `@Size`...).
- **SQL injection:** todas as consultas passam pelo JPA com parâmetros, sem concatenar SQL.
- **XSS:** texto livre dos relatos é salvo com HTML escapado.
- **Sensores:** autenticados por chave (`X-Sensor-Key`), comparada em tempo constante.
- **Erros:** respostas padronizadas, sem stack trace. O login devolve a mesma mensagem para email e senha errados.
- **Timing attack:** quando o e-mail não existe, o BCrypt ainda roda com um hash fixo gerado na inicialização, nivelando o tempo de resposta e impedindo enumeração de e-mails por diferença de latência.
- **Race condition:** lock pessimista no bairro (`PESSIMISTIC_WRITE`) e índice único condicional no Oracle impedem que leituras concorrentes gerem alertas duplicados.
- **CORS:** restrito por configuração, incluindo o método `PATCH`. Segredos (JWT, banco, chave dos sensores) vêm de variáveis de ambiente.

## 7. Uso de IA generativa

| Ferramenta | Como foi usada |
|---|---|
| **Claude (Anthropic)** | Análise do projeto, sugestões de melhorias de segurança e correção de bugs (timing attack, race condition, encerramento automático, PATCH atômico, semântica CHAR), e implementação das mudanças deste PR. Todas as sugestões e o código gerado foram revisados e validados pelo grupo antes de incorporar. |
