# v7. Estratégia Abrangente de Testes Automatizados

- **Status**: Aceito
- **Data**: 2026-09-20

## 1. Contexto (O Red-Flag)
A estabilidade de um encurtador de URLs de alta escala depende de garantias rigorosas em três áreas críticas:
1. **Algoritmo de Codificação Base62**: Risco de overflow, caracteres inválidos corrompendo decodificações, ou inconsistência matemática de ida-e-volta (*round-trip*).
2. **Camada Web e Contratos HTTP**: Falta de testes isolados validando códigos de status HTTP (201 Created, 400 Bad Request com RFC 7807, 302 Found com header `Location`, 404 Not Found).
3. **Concorrência e Condição de Corrida**: Necessidade de comprovação empírica de que requisições paralelas simultâneas convergem de forma atômica sem falhas ou registros fantasmas.

## 2. Decisão

1. **Testes Unitários do Algoritmo Base62 (`Base62Test`)**:
   - **Bordas Numéricas**: Validação de `value = 0` (retorno `"0"`), números negativos (`IllegalArgumentException`) e limite de 64 bits (`Long.MAX_VALUE`).
   - **Bordas de Entrada**: Strings nulas e caracteres especiais fora do alfabeto Base62 (`@`, `#`, espaços, etc.) rejeitados com `IllegalArgumentException`.
   - **Consistência Bidirecional**: Testes parametrizados e teste com 1.000 iterações aleatórias garantindo $decode(encode(n)) = n$ para qualquer $n \ge 0$.

2. **Testes da Camada Web com `@WebMvcTest` (`UrlControllerTest`)**:
   - Respostas de sucesso `201 Created` contendo o payload estruturado e `shortUrl` canônica.
   - Rejeição `400 Bad Request` com RFC 7807 para URLs nulas, vazias, com tamanho excedido (> 2048 chars) ou esquemas inseguros/inválidos (`ftp://`, `javascript:`).
   - Redirecionamento `302 Found` com header `Location` para códigos existentes e `404 Not Found` para códigos inexistentes.

3. **Teste de Carga Concorrente (`ShortenUrlIntegrationTest`)**:
   - Disparo simultâneo de 20 threads paralelas usando `CountDownLatch` e `ExecutorService` em bloco `try-with-resources`.
   - Asserção de que todas as threads convergem para o mesmo `shortCode` e que exatamente 1 registro é gravado no banco relacional.

## 3. Consequências

### Positivas:
- Suíte saltou para 42 testes executados com 100% de sucesso em poucos segundos.
- Confiança total na camada matemática e nas bordas do protocolo HTTP.
- Zero regressões em builds locais ou de CI/CD.
