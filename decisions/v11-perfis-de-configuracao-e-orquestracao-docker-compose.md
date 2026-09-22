# ADR v11: Isolamento de Perfis de Configuração e Orquestração com Docker Compose

## Status
Aceito

## Contexto
Para garantir a reprodutibilidade, portabilidade e segurança operacional dos ambientes de desenvolvimento, testes e execução conteinerizada, era necessário resolver:
1. **Isolamento de Banco de Testes vs Desenvolvimento**: O banco em memória H2 não deve vazar para a execução de desenvolvimento ou produção. Os testes automatizados devem utilizar um perfil dedicado (`test`) com banco H2 isolado e efêmero.
2. **Ambiente Local e Docker**: O perfil de desenvolvimento (`dev`) deve conectar-se a instâncias reais de PostgreSQL e Redis orquestradas via containers.
3. **Prevenção de Conflitos de Portas no Host**: Desenvolvedores frequentemente já possuem serviços de PostgreSQL (porta 5432) ou Redis (porta 6379) em execução em suas máquinas locais. A orquestração do `docker-compose.yaml` e as propriedades de ambiente precisam ser parametrizáveis via variáveis de ambiente com valores padrão seguros (`${DB_PORT:-5432}`, etc.).
4. **Ordem de Inicialização e Dependências Saudáveis (Health Checks)**: Em ambientes conteinerizados, a aplicação depende do banco de dados relacional e do Redis estarem prontos para aceitar conexões antes do Spring Boot inicializar seu contexto.

## Decisão

1. **Separação Estrita de Perfis de Configuração**:
   - `application.properties`: Contém apenas definições globais da aplicação e define o perfil padrão (`spring.profiles.default=dev`).
   - `application-dev.properties`: Configura o datasource PostgreSQL (`jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/url`) e o Redis (`spring.data.redis.host=${REDIS_HOST:localhost}:${REDIS_PORT:6379}`), ativados por padrão no desenvolvimento local e conteinerizado.
   - `src/test/resources/application-test.properties`: Configuração exclusiva para a suíte de testes com H2 em memória (`jdbc:h2:mem:testdb`), console H2 ativado para inspeção se necessário e validação de migrações Flyway.
   - `src/test/resources/application.properties`: Força `spring.profiles.active=test` para que qualquer execução de teste automatizado herde esse isolamento por padrão, com anotação `@ActiveProfiles("test")` explícita nos testes de contexto Spring.

2. **Dockerfile Multi-Stage**:
   - Criação de um `Dockerfile` otimizado em dois estágios (`maven:3.9-eclipse-temurin-21-alpine` para compilação e `eclipse-temurin:21-jre-alpine` para runtime enxuto e seguro).

3. **Orquestração com Docker Compose e Health Checks**:
   - PostgreSQL utilizando imagem com versão fixa (`postgres:17-alpine`) e health check nativo via `pg_isready -U url -d url`.
   - Redis utilizando `redis:7-alpine` e health check nativo via `redis-cli ping`.
   - O serviço `app` declara dependência estrita com `depends_on: { postgres: { condition: service_healthy }, redis: { condition: service_healthy } }`.
   - Mapeamento de portas flexível (`${DB_PORT:-5432}:5432`, `${REDIS_PORT:-6379}:6379`, `${APP_PORT:-8080}:8080`), permitindo alterar portas no host caso haja conflitos com serviços locais.
   - Fornecido arquivo `.env.example` com a documentação das variáveis de porta e credenciais.

## Consequências

- **Positivas**:
  - Testes rodam com máxima velocidade e isolamento sem depender de serviços externos no host.
  - Zero acoplamento entre a configuração de testes e a infraestrutura real.
  - Inicialização à prova de falhas com Docker Compose, impedindo que a aplicação Spring Boot inicialize antes que Postgres e Redis estejam saudáveis.
  - Flexibilidade total para resolver conflitos de portas em qualquer ambiente operacional.
