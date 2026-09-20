# v5. Validação de Entrada de Dados e Padronização de Erros com RFC 7807

- **Status**: Aceito
- **Data**: 2026-09-20

## 1. Contexto
A API pública recebia URLs sem validações formais no nível de entrada HTTP. Isso expunha o sistema a:
1. Payloads vazios ou maliciosos (strings em branco, nulas ou protocolos arbitrários como `javascript:`, `file:`, `ftp:`).
2. Ataques de DoS com URLs de tamanho desproporcional.
3. Respostas de erro sem padrão em caso de falhas de desserialização ou validação (`500 Internal Server Error` ou stack traces vazados).

## 2. Decisão

1. **Dependência `spring-boot-starter-validation`**:
   Integração do Bean Validation (Hibernate Validator) ao pipeline web.
2. **Restrições de Entrada no DTO `ShortenUrlRequest`**:
   - `@NotBlank`: impede strings vazias, apenas espaços ou nulas.
   - `@Size(max = 2048)`: delimita o tamanho máximo seguro de URLs conforme convenção de navegadores e servidores.
   - `@URL(regexp = "^https?://.+")`: garante formato válido de URI e restringe estritamente aos protocolos `http` e `https`.
3. **Validação Ativa no Controller**:
   - Aplicação da anotação `@Valid` no `@RequestBody` de `UrlController`.
4. **Tratamento Centralizado e Padronizado (RFC 7807)**:
   - Criação do `GlobalExceptionHandler` anotado com `@RestControllerAdvice`.
   - Interceptação de `MethodArgumentNotValidException` retornando `ProblemDetail` (RFC 7807) com HTTP `400 Bad Request`, incluindo a lista mapeada de campos e mensagens de erro (`errors: { "url": "..." }`).

## 3. Consequências

### Positivas:
- **Segurança de borda**: requisições malformadas são rejeitadas antes de alcançar o caso de uso ou o banco de dados.
- **Padronização RFC 7807**: respostas de erro previsíveis e estruturadas para clientes HTTP.
- **Cobertura automatizada**: testes de unidade no Controller com `MockMvc` garantindo comportamento para múltiplos cenários (strings vazias, URLs longas e protocolos inválidos).

### Negativas / Trade-offs:
- Clientes que enviarem URLs sem esquema explícito (`exemplo.com` em vez de `https://exemplo.com`) receberão `400 Bad Request`, exigindo URL canônica completa.
