# Publicação do Poke Fichas

A aplicação é dividida em dois serviços:

- `web-poke-fichas`: Angular e proxy Node na Vercel.
- `api-poke-fichas`: Spring Boot 4/Java 21 em um host com suporte a Docker, ligado ao PostgreSQL.

## 1. Banco e backend

Crie um PostgreSQL gerenciado e publique `api-poke-fichas` usando o `Dockerfile` da pasta. Configure:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://HOST:5432/BANCO
DATABASE_USERNAME=USUARIO
DATABASE_PASSWORD=SENHA_FORTE
JWT_SECRET=CHAVE_ALEATORIA_COM_32_BYTES_OU_MAIS
CORS_ALLOWED_ORIGIN_PATTERNS=https://SEU-PROJETO.vercel.app
SWAGGER_ENABLED=false
```

O host deve encaminhar a porta fornecida em `PORT`. A API expõe o healthcheck em `/api/actuator/health` e executa as migrations Flyway ao iniciar.

O valor de `DATABASE_URL` precisa usar o formato JDBC. Se o provedor entregar `postgresql://...`, prefixe com `jdbc:`.

## 2. Frontend na Vercel

Crie o projeto com:

```text
Root Directory: web-poke-fichas
Framework Preset: Angular
Build Command: npm run build
Output Directory: dist/web-poke-fichas/browser
```

Variável obrigatória:

```text
BACKEND_API_URL=https://URL-PUBLICA-DO-BACKEND
```

Pode ser informado com ou sem o sufixo `/api`.

## 3. Ordem recomendada

1. Publique PostgreSQL e backend.
2. Confirme resposta `UP` em `/api/actuator/health`.
3. Cadastre `BACKEND_API_URL` na Vercel.
4. Publique o frontend.
5. Registre a primeira conta pela tela do site.
6. Teste login, refresh de sessão, criação de ficha, upload de imagem e URL pública da ficha.

## Segredos

Não publique arquivos `.env`. Gere `JWT_SECRET` com um gerenciador de senhas ou ferramenta criptográfica e mantenha Swagger desativado em produção.
