# Contrato da API (Endpoints)

Especificação detalhada de requisições, respostas e tratamento de erros do Encurtador de URLs.

---

## 1. Encurtar URL
`POST /api/v1/urls`

Gera um identificador único encurtado utilizando pré-geração de TSID e codificação Base62.

### Requisição
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.google.com"}'
```

### Resposta de Sucesso (`HTTP 201 Created`)
```json
{
  "id": 890052421229969499,
  "originalUrl": "https://www.google.com",
  "shortCode": "13KrS03YU4P",
  "shortUrl": "http://localhost:8080/13KrS03YU4P"
}
```

---

## 2. Redirecionamento
`GET /{shortCode}`

Redireciona imediatamente para a URL original com cache-aside via Redis.

### Requisição
```bash
curl -v http://localhost:8080/13KrS03YU4P
```

### Resposta de Sucesso (`HTTP 302 Found`)
```http
HTTP/1.1 302 Found
Location: https://www.google.com
```

---

## 3. Estatísticas e Métrica de Cliques
`GET /api/v1/urls/{shortCode}/stats`

Retorna contadores de engajamento consolidados no PostgreSQL com telemetria assíncrona.

### Requisição
```bash
curl http://localhost:8080/api/v1/urls/13KrS03YU4P/stats
```

### Resposta de Sucesso (`HTTP 200 OK`)
```json
{
  "shortCode": "13KrS03YU4P",
  "originalUrl": "https://www.google.com",
  "totalClicks": 42,
  "createdAt": "2026-09-21T22:50:25Z"
}
```

---

## 4. Tratamento de Erros (RFC 7807 ProblemDetail)

A API padroniza respostas de falhas seguindo a especificação RFC 7807.

### Exemplo: URL Inválida ou em Branco (`HTTP 400 Bad Request`)
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Parâmetros inválidos na requisição.",
  "instance": "/api/v1/urls",
  "errors": {
    "url": "deve ser uma URL válida"
  }
}
```

### Exemplo: Código Não Encontrado (`HTTP 404 Not Found`)
```http
HTTP/1.1 404 Not Found
```
