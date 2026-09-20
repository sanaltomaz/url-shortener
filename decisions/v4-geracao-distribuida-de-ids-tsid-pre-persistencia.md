# v4. Geração de ID Distribuído Pré-Persistência (TSID) e Eliminação do Duplo Round-Trip

- **Status**: Aceito
- **Data**: 2026-09-20

## 1. Contexto (O Red-Flag)
Na versão inicial do caso de uso de encurtamento, utilizava-se a estratégia de ID sequencial gerado pelo banco (`GenerationType.IDENTITY`). Isso causava três problemas críticos de arquitetura e desempenho:

1. **Duplo Round-trip ao Banco (`INSERT` + `UPDATE`)**:
   O caso de uso precisava primeiro salvar a entidade sem `shortCode` para obter o ID sequencial gerado pelo banco, codificar o ID em Base62 e depois executar um `UPDATE` para gravar o `shortCode`.
2. **Inconsistência de Schema**:
   A coluna `shortCode` precisava aceitar valores nulos (`nullable = true`) para permitir o primeiro `INSERT`.
3. **Gargalo de Concorrência do `IDENTITY`**:
   O `GenerationType.IDENTITY` impede o uso de *JDBC batch inserts* no Hibernate e introduz contenção de locks na tabela sob alta taxa de escrita concorrente.

## 2. Decisão
Substituir a geração de IDs pelo banco por um **gerador de IDs distribuído pré-persistência baseado em TSID (Time-Sorted Unique Identifier)** via biblioteca `hypersistence-tsid`.

### Detalhes Técnicos da Implementação:
1. **Contrato de Domínio**:
   Criada a interface `IdGenerator` no pacote `domain` com a implementação `TsidGenerator` na camada `infrastructure`.
2. **Geração Pré-Persistência**:
   O caso de uso gera o `id` (64 bits) e o `shortCode` em memória antes de invocar o repositório. O objeto de domínio nasce completo e consistente.
3. **Persistência em Única Operação**:
   A entidade `UrlEntity` passa a receber o ID pré-atribuído, permitindo que a coluna `shortCode` seja `@Column(nullable = false, unique = true)`.
4. **Otimização `Persistable<Long>`**:
   A `UrlEntity` implementa a interface `Persistable<Long>` do Spring Data com controle da flag `isNew` (`@Transient` e ganchos `@PrePersist`/`@PostLoad`). Isso evita que o Spring Data JPA execute uma query `SELECT` desnecessária (`em.merge()`) antes de fazer o `INSERT` direto (`em.persist()`).

## 3. Alternativas Consideradas

- **Sequência PostgreSQL em Lote (Hi/Lo / Sequence Pool)**:
  Alocaria lotes (ex: 1.000 IDs) na memória da JVM vindos de uma sequence do Postgres. Descartada por manter dependência do banco para alocação de blocos e risco de lacunas em reinicializações.
- **Hash da URL (Murmur3 / SHA-256) com Salt**:
  Descartada pelo risco de colisões de hash que exigiriam loops de retry complexos e captura de `DataIntegrityViolationException`.
- **Snowflake (Twitter)**:
  Descartada pela complexidade operacional de gerenciar `workerId` e nós de coordenação quando o TSID entrega os mesmos benefícios sem infraestrutura adicional.

## 4. Consequências

### Positivas:
- **Redução de 50% nas operações de banco**: de 2 operações (`INSERT` + `UPDATE`) para apenas 1 `INSERT`.
- **Schema estrito**: integridade garantida com `NOT NULL` e `UNIQUE` no `short_code`.
- **Desempenho distribuído**: IDs gerados na JVM em nanossegundos (`TSID.fast()`) sem locks e sem comunicação de rede.
- **Ordenação temporal (B-Tree friendly)**: como o TSID é ordenado por tempo nos primeiros 42 bits, os índices da chave primária sofrem muito menos fragmentação no PostgreSQL.

### Negativas / Trade-offs:
- Os códigos gerados em Base62 passam a ter entre 10 e 11 caracteres (representação de 64 bits), ao invés de iniciarem muito pequenos (1 a 4 chars) como em sequências numéricas incrementais.
