# Poke Fichas API

Backend Spring Boot para gerenciamento de fichas de treinador inspiradas no modelo do PDF usado como referencia.

O projeto foi criado a partir dos padroes do backend de referencia:

- Java 21 e Spring Boot.
- Controllers finos em `infra.web.controller`.
- Um use case por operacao.
- Repositorios separados em `Query` e `Command`.
- Entidades JPA com `Long` identity, auditoria e builder interno.
- Migrations Flyway versionadas.
- Requests com Bean Validation e JSON em `camelCase`.
- Tratamento de erros centralizado no `GlobalExceptionHandler`.

## Requisitos

- Java 21
- Maven 3.9+
- Docker

## Banco local

Subir banco e API com Docker Compose:

```powershell
docker compose up --build
```

Subir apenas o banco:

```powershell
docker compose up -d postgres
```

## Rodar localmente

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

A API sobe em:

- `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI: `http://localhost:8080/api/api-docs`

## Produção

O backend deve ser publicado em um serviço com Java 21 ou Docker e PostgreSQL persistente. A Vercel hospeda o frontend e encaminha `/api` para este serviço.

Use o perfil `prod` e configure as variáveis de `.env.example`. As obrigatórias são:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://HOST:5432/BANCO
DATABASE_USERNAME=USUARIO
DATABASE_PASSWORD=SENHA
JWT_SECRET=CHAVE_ALEATORIA_COM_PELO_MENOS_32_BYTES
```

Healthcheck: `GET /api/actuator/health`.

Não use `application-local.yml` em produção. Ele contém apenas valores destinados ao desenvolvimento local.

## Fluxo minimo

1. Execute o bootstrap inicial em `POST /api/bootstrap`.
2. Faca login em `POST /api/auth/login`.
3. Use o `accessToken` retornado no header `Authorization`.
4. Use os endpoints de ficha:

```http
GET /api/fichas
GET /api/fichas/{id}
POST /api/fichas
PUT /api/fichas/{id}
```

## Modelo coberto

A ficha principal contempla:

- dados do treinador: nome, frase, idade, naturalidade, classe, altura, peso, tipo fisico, indole, ranking, ocupacao, reputacao, dinheiro, pontos de vida, equipe, pontos, photoplayer, player e biografia;
- relacionados;
- habilidades;
- conquistas por tipo;
- Pokemon com dados, stats, atributos de contest e movimentos;
- itens por categoria de bolsa;
- anotacoes e registros.

O personagem do PDF foi usado apenas para identificar os campos; nenhum dado dele foi inserido como seed.
