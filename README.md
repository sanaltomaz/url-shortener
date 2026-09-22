# Encurtador de URLs com Cache e Métricas

> O foco deste projeto são leituras de alta velocidade com baixa latência, integridade de dados e simplicidade de execução local.

## 🎯 Objetivo do Projeto

Uma API que recebe uma URL longa, gera um identificador único encurtado (ex.: `app.io/aB3x9`), redireciona requisições via código de status HTTP `302` ou `301`, e registra métricas de acesso.

## 💡 Problema Real que Resolve

Redução do tamanho de links para fácil compartilhamento, mascaramento de parâmetros de rastreamento e medição de engajamento de cliques sem degradar o tempo de resposta da rota principal de redirecionamento.

## 🚀 Como Instalar e Rodar

### Pré-requisitos
- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/)
- *(Opcional para rodar sem container)*: Java 21+ e Maven 3.9+

---

### Opção 1: Rodando 100% via Docker (Recomendado)

Sobe toda a stack (PostgreSQL, Redis e aplicação Spring Boot) orquestrada com health checks:

1. **(Opcional) Configurar variáveis de porta**:
   Caso sua máquina host já utilize as portas padrão (`5432` ou `6379`), copie o arquivo de exemplo e customize:
   ```bash
   cp .env.example .env
   ```

2. **Iniciar os serviços**:
   ```bash
   docker compose up --build -d
   ```

3. **Acompanhar os logs**:
   ```bash
   docker compose logs -f app
   ```

4. **Encerrar a stack**:
   ```bash
   docker compose down
   ```

---

### Opção 2: Rodando a aplicação localmente (com dependências no Docker)

Caso prefira rodar a aplicação Spring Boot pelo terminal ou IDE e usar apenas o PostgreSQL e o Redis em containers:

1. **Subir apenas Postgres e Redis**:
   ```bash
   docker compose up -d postgres redis
   ```

2. **Executar a aplicação**:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(O perfil `dev` é ativado por padrão, conectando-se a `localhost:5432` e `localhost:6379`).*

---

### Opção 3: Executar a suíte de testes automatizados

Os testes rodam isolados utilizando banco H2 em memória e o perfil `test`, sem necessidade de containers ativos:

```bash
./mvnw clean test
```

---

## 🛠️ Endpoints Principais

- [x] `POST /api/v1/urls` — Gera o código encurtado com validação de formato e tamanho.
- [x] `GET /{shortCode}` — Realiza o redirecionamento imediato (`302 Found`) para a URL original.
- [x] `GET /api/v1/urls/{shortCode}/stats` — Retorna estatísticas de acesso e total de cliques.

## 🛠️ Conceitos e Práticas-Chave

- [x] **Algoritmo de Codificação:** Conversão de IDs numéricos distribuídos (TSID) em Base62 (evita colisões de hash MD5/SHA256 sem truncamento inseguro).
- [x] **Camada de Cache com Redis:** Armazenamento chave-valor (`shortCode` &rarr; `originalUrl`) via padrão Cache-Aside para servir redirecionamentos sem consultar o banco relacional a cada requisição.
- [x] **Métricas Assíncronas:** Incremento atômico de acessos via Redis `INCR` e publicação assíncrona de eventos (`@Async`), sincronizados periodicamente para a base de dados relacional.
- [x] **Migrations de Banco de Dados:** Evolução e versionamento de schema com Flyway, utilizando estratégia `validate` no Hibernate.
- [x] **Conteinerização e Perfis:** `docker-compose.yaml` contendo a aplicação, PostgreSQL e Redis configurados com health checks e isolamento estrito de perfis (`test` vs `dev`).

## 📚 Decisões de Arquitetura (ADRs)

Consulte [decisions/README.md](./decisions/README.md) para detalhes técnicos e justificativas arquiteturais (v1 a v11).
