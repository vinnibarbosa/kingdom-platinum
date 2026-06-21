# Poke Fichas - Frontend

Aplicação Angular 18 para criação, edição e visualização pública de fichas.

## Desenvolvimento local

Requisitos: Node.js 20.11+ e API em `http://127.0.0.1:8080/api`.

```powershell
npm install
npm run dev
```

O proxy local encaminha `/api` para a API Spring.

## Verificações

```powershell
npm run check
npm run build
```

## Publicação na Vercel

1. Crie um projeto apontando a raiz para `web-poke-fichas`.
2. Use o preset Angular. O `vercel.json` já define build e output.
3. Cadastre `BACKEND_API_URL` com a URL pública do backend, por exemplo `https://pokefichas-api.onrender.com`.
4. Publique novamente após salvar a variável.

O arquivo `api/[...path].mjs` funciona como proxy same-origin. O navegador continua chamando `/api`, e cookies de refresh permanecem no domínio da Vercel.
