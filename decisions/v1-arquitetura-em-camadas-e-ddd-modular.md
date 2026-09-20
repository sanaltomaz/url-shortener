# v1. Arquitetura em Camadas e Isolamento do Domínio

- **Status**: Aceito
- **Data**: 2026-09-20

## 1. Contexto
O projeto necessita de uma estrutura desacoplada que permita evolução independente de frameworks web, bancos de dados e regras de negócio. O encurtador de URLs terá componentes críticos como algoritmos de codificação, geração de IDs, camadas de cache e métricas assíncronas.

## 2. Decisão
Adotar a separação em camadas inspirada em Arquitetura Limpa / Hexagonal (Ports & Adapters) e DDD tático no pacote `com.tom.url_shortener.url`:

- **`domain`**: Entidades puras e contratos de portas (`Url`, `UrlRepository`, `IdGenerator`), sem acoplamento com anotações de frameworks (JPA, Spring Web).
- **`application`**: Casos de uso (`ShortenUrlUseCase`, `GetOriginalUrlUseCase`) e DTOs/Commands que orquestram fluxos de negócio.
- **`infrastructure`**: Adaptadores secundários e primários (controllers REST, entidades JPA, mappers, utilitários de codificação e implementações de banco de dados).

## 3. Consequências

### Positivas:
- As regras de negócio e casos de uso podem ser testados com mocks puros de forma extremamente rápida.
- Trocas de banco de dados, bibliotecas ou estratégias de infraestrutura não vazam para o domínio da aplicação.
- Código limpo, coeso e de fácil manutenção.

### Negativas / Trade-offs:
- Exige mappers explícitos entre objetos de domínio (`Url`) e entidades de persistência (`UrlEntity`).
- Pequena sobrecarga inicial de arquivos e interfaces.
