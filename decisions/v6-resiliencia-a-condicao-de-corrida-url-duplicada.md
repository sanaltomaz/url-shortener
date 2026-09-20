# v6. Resiliência a Condições de Corrida em URLs Duplicadas

- **Status**: Aceito
- **Data**: 2026-09-20

## 1. Contexto (O Red-Flag)
Em cenários de concorrência (ex.: múltiplas requisições paralelas enviando a mesma URL original no mesmo milissegundo):
1. O padrão *check-then-act* (`findByOriginalUrl` seguido de `createNewUrl`) apresentava uma **janela de corrida (race condition)**. Ambas as requisições verificavam que a URL não existia e tentavam inseri-la simultaneamente.
2. A coluna `original_url` na tabela `tb_urls` não possuía uma restrição de unicidade (`UNIQUE`), permitindo que a mesma URL original fosse salva múltiplas vezes com códigos curtos diferentes, quebrando a integridade e a idempotência.
3. Caso a constraint existisse mas não fosse tratada pela aplicação, a requisição concorrente falharia com `500 Internal Server Error` devido ao estouro de `DataIntegrityViolationException`.

## 2. Decisão

1. **Restrição de Unicidade no Banco de Dados (`UrlEntity`)**:
   - A coluna `originalUrl` foi anotada com `@Column(nullable = false, unique = true, length = 2048)`.
   - Garante no nível do banco de dados (índice único) que registros duplicados sejam terminantemente impedidos.

2. **Tratamento Resiliente de Concorrência (`ShortenUrlUseCase`)**:
   - O caso de uso captura especificamente `DataIntegrityViolationException` durante o `urlRepository.save(url)`.
   - Ao detectar conflito de chave única por requisições paralelas, a aplicação faz um fallback automático para `urlRepository.findByOriginalUrl(originalUrl)`.
   - A requisição concorrente recupera o registro salvo pela primeira thread e retorna a resposta com sucesso (HTTP `201`), mantendo **idempotência estrita** e eliminando falhas 500 para o cliente.

3. **Remoção de `@Transactional` Abrangente no Use Case**:
   - O método `execute` deixou de englobar todo o fluxo em uma única transação de nível superior.
   - Isso evita o problema de *rollback-only* do Hibernate, permitindo que uma nova consulta limpa seja realizada imediatamente após o conflito de inserção ser contornado.

## 3. Consequências

### Positivas:
- **Idempotência garantida**: requisições simultâneas para a mesma URL sempre convergem para o mesmo `shortCode`.
- **Integridade física**: o banco de dados rejeita anomalias mesmo que chamadas bypassassem a camada de aplicação.
- **Zero erros 500 em alta concorrência**: comprovado por testes automatizados com threads paralelas disparadas simultaneamente (`CountDownLatch`).

### Negativas / Trade-offs:
- O índice único no campo `original_url` adiciona um pequeno custo de escrita no B-Tree do banco, plenamente justificado pela necessidade de integridade e buscas rápidas por URL original.
