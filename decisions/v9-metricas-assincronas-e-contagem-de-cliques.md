# v9. Métricas Assíncronas e Contagem de Cliques com Redis INCR e Agregação

## Status
**Aceito**

## Contexto
O redirecionamento de URLs (`GET /{shortCode}`) é o caminho crítico (hot path) com maior volume de requisições de um encurtador de URLs. Executar escrita direta no banco relacional (`UPDATE tb_urls SET click_count = click_count + 1 WHERE short_code = ...`) a cada requisição de redirecionamento geraria:
1. **Contenção e Lock de Linha**: Requisições simultâneas ao mesmo link sofreriam com locks de escrita no PostgreSQL.
2. **Degradação de Latência**: A latência do redirecionamento aumentaria de submilisegundos para dezenas de milissegundos devido ao I/O de disco do banco relacional.
3. **Ponto de Falha e Gargalo**: O throughput máximo da aplicação ficaria limitado pela taxa de escrita do banco relacional.

Além disso, a API precisa fornecer estatísticas confiáveis sobre as URLs encurtadas via endpoint `GET /api/v1/urls/{shortCode}/stats`, retornando a URL original, shortCode, total de cliques e data de criação (`createdAt`).

## Decisão
1. **Telemetria Não-Bloqueante via Eventos Internos do Spring**:
   - No caso de uso de redirecionamento (`GetOriginalUrlUseCase`), ao resolver a URL com sucesso (seja via cache HIT ou DB miss), é publicado um evento de domínio desacoplado: `UrlClickedEvent(shortCode)`.
   - O use-case responde o redirecionamento HTTP 302 imediatamente, sem qualquer espera.

2. **Incremento Atômico no Redis (`INCR`)**:
   - Um listener assíncrono (`@Async` / `@EventListener`) captura o evento `UrlClickedEvent` em uma thread pool separada.
   - O adaptador `RedisUrlMetricsRepository` executa a operação atômica de incremento `redisTemplate.opsForValue().increment("url:clicks:" + shortCode)`.
   - A operação `INCR` no Redis é executada em memória em tempo O(1) e é nativamente thread-safe e atômica.
   - O adaptador inclui tratamento de falhas resiliente (*graceful degradation*): caso o Redis esteja indisponível, a falha é logada como aviso e a requisição principal não é afetada.

3. **Sincronização e Agregação Híbrida**:
   - **Job Agendado (`ClickMetricsSyncScheduler`)**: Periodicamente (`@Scheduled`), um processo em background sincroniza os contadores de cliques em memória do Redis com a coluna `click_count` da tabela relacional `tb_urls`.
   - **Consulta de Estatísticas (`GetUrlStatsUseCase`)**: Ao consultar `GET /api/v1/urls/{shortCode}/stats`:
     - Valida se o `shortCode` existe no repositório persistente (retornando `404 Not Found` caso contrário).
     - Agrega os dados calculando o total consolidado (`Math.max(dbClicks, redisClicks)`), garantindo precisão mesmo que o job agendado ainda não tenha executado ou caso o Redis tenha sido reiniciado.

## Consequências e Trade-offs

### Benefícios
- **Latência Praticamente Zero no Redirecionamento**: A gravação de métricas não bloqueia o envio da resposta HTTP 302 ao cliente final.
- **Zero Lock no Banco de Dados**: A concorrência extrema de cliques em um mesmo link viral é absorvida com facilidade pela estrutura de contadores atômicos em memória do Redis.
- **Tolerância a Falhas**: Se o Redis sofrer uma instabilidade temporária, o redirecionamento continua funcionando e a contagem consolidada no banco relacional preserva o histórico já persistido.
- **Desacoplamento Arquitetural**: A publicação de eventos segue a Clean Architecture, permitindo no futuro plugar novos consumidores (como Kafka, dashboards ou pipelines de telemetria) sem alterar o core do encurtador.

### Trade-offs Aceitos
- **Consistência Eventual na Tabela Relacional**: Os dados na tabela `tb_urls` possuem um delay de sincronização relativo ao intervalo do scheduler (`app.metrics.sync-interval-ms`). Porém, as consultas feitas pela API agregam a memória do Redis, provendo visão em tempo real para o usuário final.
