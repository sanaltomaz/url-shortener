# v3. Persistência Relacional e Mapeamento de Entidades

- **Status**: Aceito
- **Data**: 2026-09-20

## 1. Contexto
A aplicação precisa armazenar de forma durável e com garantias ACID o relacionamento entre URLs originais e seus códigos encurtados, além de garantir a unicidade do código para evitar redirecionamentos conflitantes.

## 2. Decisão
Adotar um banco de dados relacional com **Spring Data JPA / Hibernate**:
- **PostgreSQL** para ambiente de produção/containers via `docker-compose.yaml`.
- **H2 in-memory** para testes rápidos automatizados e desenvolvimento local ágil.
- Criação da tabela `tb_urls` com restrição de chave primária (`id`) e índice único no campo `short_code`.

## 3. Consequências

### Positivas:
- Integridade referencial forte e garantias transacionais imediatas.
- Índices B-Tree eficientes para recuperação por `short_code` com complexidade $O(\log n)$.
- Suporte maduro do ecossistema Spring Boot Data JPA.

### Negativas / Trade-offs:
- O banco relacional exige cuidados com conexões de pool (HikariCP) e estratégias de lock sob alta carga de escrita concorrente.
