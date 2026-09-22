# v10. Versionamento de Banco de Dados com Migrations (Flyway)

- **Status**: Aceito
- **Data**: 2026-09-21

## 1. Contexto
Anteriormente, o schema do banco relacional dependia de `spring.jpa.hibernate.ddl-auto=update`, o que delegava a criação e alteração de tabelas diretamente ao Hibernate em tempo de execução da aplicação. Essa abordagem apresenta sérios riscos operacionais em ambientes de homologação e produção:
- Dificuldade de rastrear histórico e mudanças estruturais no schema ao longo do tempo.
- Risco de alterações destrutivas ou divergências de schema entre instâncias e ambientes.
- Falta de controle fino sobre índices, restrições e tipos exatos de dados SQL nativos.

## 2. Decisão
Adotamos o **Flyway** para gerenciamento evolutivo e versionamento estrito de migrations do banco de dados:
- Adição das dependências `org.springframework.boot:spring-boot-flyway`, `org.flywaydb:flyway-core` e `org.flywaydb:flyway-database-postgresql` (além de `postgresql` driver).
- Criação do script de migration inicial [V1__create_urls_table.sql](../src/main/resources/db/migration/V1__create_urls_table.sql) padronizando a tabela `urls` com ANSI SQL compatível tanto com PostgreSQL quanto com H2:
  - `id BIGINT PRIMARY KEY`
  - `original_url VARCHAR(2048) NOT NULL UNIQUE`
  - `short_code VARCHAR(255) NOT NULL UNIQUE`
  - `created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `click_count BIGINT NOT NULL DEFAULT 0`
- Atualização do mapeamento da entidade [UrlEntity.java](../src/main/java/com/tom/url_shortener/url/infrastructure/persistence/entity/UrlEntity.java) para `@Table(name = "urls")`.
- Alteração da configuração do JPA de `ddl-auto=update` para `ddl-auto=validate` nos perfis de desenvolvimento e produção: o Hibernate valida a compatibilidade estrita do schema gerado pelo Flyway sem criar ou alterar tabelas de forma arbitrária.

## 3. Consequências

### Positivas:
- **Reprodutibilidade e Determinismo**: Todas as instâncias e pipelines de CI/CD executam os mesmos scripts SQL ordenados e idempotentes via tabela `flyway_schema_history`.
- **Segurança Operacional**: `ddl-auto=validate` garante que a aplicação só suba se o banco de dados estiver na versão exata esperada pelo código.
- **Histórico Auditável**: Mudanças no modelo de dados ficam versionadas no controle de código-fonte (Git).

### Negativas / Trade-offs:
- Qualquer alteração na entidade necessita da criação explícita de um novo script de migration (`V2__...sql`), exigindo disciplina no ciclo de desenvolvimento.
