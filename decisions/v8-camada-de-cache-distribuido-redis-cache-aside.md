# v8. Camada de Cache Distribuído com Redis (Cache-Aside)

- **Status**: Aceito
- **Data**: 2026-09-20

## 1. Contexto
Encurtadores de URL possuem um padrão de tráfego desproporcional (*read-heavy*), onde consultas de redirecionamento (`GET /{shortCode}`) superam as operações de encurtamento em ordens de grandeza. Consultar o banco relacional (PostgreSQL) a cada redirecionamento gera gargalos de I/O, saturação do pool de conexões (HikariCP) e eleva a latência p99.

## 2. Decisão

Adotamos a estratégia **Cache-Aside** (Lazy Loading) com aquecimento antecipado no encurtamento, utilizando **Redis**:

1. **Port e Adaptador na Arquitetura Limpa**:
   - `UrlCacheRepository` definido na camada de domínio (`domain/UrlCacheRepository.java`).
   - `RedisUrlCacheRepository` implementado em `infrastructure/cache`, isolando o Spring Data Redis e Lettuce do domínio da aplicação.
2. **Configuração e Serialização (`RedisConfig`)**:
   - `StringRedisSerializer` configurado para chaves e valores, garantindo armazenamento textual puro e chave legível com namespace (`url:{shortCode}`).
   - TTL padrão de 24 horas configurável via propriedades de ambiente (`app.cache.redis.ttl=24h`).
3. **Fluxo de Leitura no Redirecionamento (`GetOriginalUrlUseCase`)**:
   - **Cache HIT**: Retorna o destino imediatamente a partir da memória do Redis (sub-millisecond latency), sem acionar o banco relacional.
   - **Cache MISS**: Consulta o repositório persistente (PostgreSQL) e, caso encontrado, grava a chave no Redis com o TTL configurado antes de responder ao cliente.
4. **Aquecimento Preventivo na Escrita (`ShortenUrlUseCase`)**:
   - No momento em que uma nova URL é criada ou reaproveitada, a tupla `(shortCode, originalUrl)` é imediatamente gravada no Redis, eliminando o *first-access cache miss*.
5. **Resiliência e Degradação Graciosa**:
   - O `RedisUrlCacheRepository` encapsula falhas transitórias de conexão (`RedisConnectionFailureException`) como logs de alerta (`WARN`), permitindo que a aplicação faça *fallback* automático e transparente para o banco relacional sem quebrar as requisições dos usuários.

## 3. Consequências

### Positivas:
- Redução drástica da carga de I/O no banco relacional em cenários de alta concorrência e URLs virais.
- Latência de leitura reduzida para a ordem de 1 a 3 milissegundos.
- Resiliência total: indisponibilidade do Redis degrada o sistema para consulta direta no banco sem interrupção de serviço (zero 500 para os clientes).

### Trade-offs:
- Introdução de mais uma dependência de infraestrutura em execução (`redis:7-alpine` no `docker-compose.yaml`).
