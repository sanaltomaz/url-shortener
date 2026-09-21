package com.tom.url_shortener.url.infrastructure.metrics;

import com.tom.url_shortener.url.domain.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickMetricsSyncScheduler {

    private final UrlRepository urlRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(
            fixedRateString = "${app.metrics.sync-interval-ms:60000}",
            initialDelayString = "${app.metrics.sync-initial-delay-ms:60000}"
    )
    public void syncClicksToDatabase() {
        try {
            Set<String> keys = redisTemplate.keys(RedisUrlMetricsRepository.CLICKS_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            for (String key : keys) {
                String shortCode = key.substring(RedisUrlMetricsRepository.CLICKS_KEY_PREFIX.length());
                String countStr = redisTemplate.opsForValue().get(key);
                if (countStr != null) {
                    long clicks = Long.parseLong(countStr);
                    urlRepository.updateClickCount(shortCode, clicks);
                }
            }
            log.debug("Métricas de cliques sincronizadas com sucesso para {} chaves.", keys.size());
        } catch (Exception ex) {
            log.warn("Falha ao sincronizar métricas de cliques do Redis com o banco: {}", ex.getMessage());
        }
    }
}
