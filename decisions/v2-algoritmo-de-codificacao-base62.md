# v2. Algoritmo de Codificação de Códigos Curtos com Base62

- **Status**: Aceito
- **Data**: 2026-09-20

## 1. Contexto
Para encurtar links, é necessário converter identificadores numéricos ou hashes em sequências alfanuméricas compactas e seguras para URLs (URL-safe), que possam ser facilmente digitadas e compartilhadas por usuários.

## 2. Decisão
Adotar o algoritmo de codificação **Base62** utilizando o conjunto de caracteres:
`0-9`, `a-z`, `A-Z` (totalizando 62 símbolos possíveis).

O componente `Base62` foi implementado para fornecer conversão bidirecional entre inteiros de 64 bits (`long`) e strings Base62.

## 3. Alternativas Consideradas

- **Base64**: Inclui caracteres especiais como `+`, `/` e `=` (padding), que requerem URL encoding percentual (`%2B`, etc.) ou variantes URL-safe (`-`, `_`), aumentando a complexidade em navegadores.
- **Base36 / Hexadecimal (Base16)**: Resulta em códigos significativamente mais longos para o mesmo intervalo numérico.
- **MD5 / SHA-256 com truncamento**: Risco constante de colisão de hash por truncamento e dependência de hashes criptográficos pesados.

## 4. Consequências

### Positivas:
- Representação ultra-compacta (um número de 64 bits ocupa no máximo 11 caracteres).
- 100% compatível com URLs sem necessidade de escape de caracteres especiais.
- Conversão matemática direta e de altíssimo desempenho (operações aritméticas de divisão e módulo).

### Negativas / Trade-offs:
- É sensível a maiúsculas e minúsculas (*case-sensitive*), exigindo que a busca no banco trate a unicidade e collation adequadamente.
