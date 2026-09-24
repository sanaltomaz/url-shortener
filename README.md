# Encurtador de URLs com Cache Distribuído e Telemetria Assíncrona

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker_Compose-Pronto-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Testes](https://img.shields.io/badge/Testes-61%20Aprovados-brightgreen?style=for-the-badge)]()
[![ADRs](https://img.shields.io/badge/ADRs-11%20Documentadas-blueviolet?style=for-the-badge)](./decisions/README.md)

> API de alto throughput para encurtamento de links e telemetria de cliques em tempo real, projetada com foco em leituras de baixíssima latência, integridade sob alta concorrência e tolerância a falhas.

---

## 🎯 Destaques do Projeto

- **Leituras em Sub-milissegundos:** Redirecionamento HTTP 302 servido via **Redis (Cache-Aside)** com fallback automático e transparente para o PostgreSQL caso o cluster de cache fique indisponível.
- **Telemetria Assíncrona:** Incrementos atômicos (`INCR`) no Redis acionados por eventos assíncronos (`@Async`), consolidados periodicamente em lote no banco relacional via agendador (`@Scheduled`), evitando contenção de I/O na rota principal.
- **Zero Round-Trips Duplos:** Identificadores temporais pré-gerados em memória via **TSID** e codificados em **Base62** bijetivo antes da persistência, necessitando de apenas um único `INSERT`.
- **Resiliente a Concorrência:** Tratamento de colisões simultâneas de URLs duplicadas com constraint única e fallback idempotente (validado com suite de 20 threads paralelas).
- **Ambiente Determinístico:** Versionamento de schema com **Flyway** (`validate`), 61 testes automatizados (unitários, integração e WebMvc) e orquestração completa via **Docker Compose**.

👉 **[Visualizar Diagrama de Arquitetura e Fluxo de Dados (docs/architecture.md)](./docs/architecture.md)**

---

## 📡 Endpoints da API

| Método | Rota | Descrição | Status |
| :---: | :--- | :--- | :---: |
| `POST` | `/api/v1/urls` | Encurta uma URL longa (validação de formato e tamanho) | `201 Created` |
| `GET` | `/{shortCode}` | Redirecionamento imediato para a URL original | `302 Found` |
| `GET` | `/api/v1/urls/{shortCode}/stats` | Estatísticas de acesso e total de cliques acumulados | `200 OK` |

👉 **[Consulte a documentação completa dos endpoints com exemplos de cURL e RFC 7807 (docs/api.md)](./docs/api.md)**

---

## 📚 Decisões de Arquitetura (ADRs)

Todas as 11 decisões técnicas, contextos e trade-offs adotados no projeto estão formalmente registrados:

👉 **[Acessar o Índice Completo de ADRs (decisions/README.md)](./decisions/README.md)**

| ADR | Decisão Chave | Motivo do Trade-off |
| :---: | :--- | :--- |
| [**v4**](./decisions/v4-geracao-distribuida-de-ids-tsid-pre-persistencia.md) | **TSID pré-persistência** | Evita múltiplos round-trips ao banco e elimina overhead de auto-increment. |
| [**v6**](./decisions/v6-resiliencia-a-condicao-de-corrida-url-duplicada.md) | **Fallback idempotente** | Garante integridade em corrida sem travar o pool de conexões com lock distribuído. |
| [**v8**](./decisions/v8-camada-de-cache-distribuido-redis-cache-aside.md) | **Cache-Aside Fail-Safe** | Permite leitura em sub-milissegundos com fallback seguro se o Redis falhar. |
| [**v9**](./decisions/v9-metricas-assincronas-e-contagem-de-cliques.md) | **Telemetria `@Async` + Lote** | Não penaliza a rota de redirecionamento com escrita em disco. |
| [**v10**](./decisions/v10-versionamento-de-banco-de-dados-com-flyway.md) | **Flyway (`validate`)** | Elimina DDL automático e assegura consistência entre ambientes. |

---

## 🚀 Como Executar

### 1. Stack Completa via Docker Compose (Recomendado)
Sobe aplicação, PostgreSQL 17 e Redis 7 com health checks:
```bash
docker compose up --build -d
```

### 2. Rodando Localmente com Dependências no Docker
```bash
docker compose up -d postgres redis
./mvnw spring-boot:run
```

### 3. Suíte de Testes Automatizados (61 testes)
Os testes rodam isolados com perfil `test` e banco H2 em memória:
```bash
./mvnw clean test
```
