package com.tom.url_shortener.url.infrastructure.metrics;

import com.tom.url_shortener.url.domain.UrlMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUrlMetricsRepository implements UrlMetricsRepository {

    public static final String CLICKS_KEY_PREFIX = "url:clicks:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public long incrementClicks(String shortCode) {
        try {
            Long count = redisTemplate.opsForValue().increment(CLICKS_KEY_PREFIX + shortCode);
            return count != null ? count : 0L;
        } catch (Exception ex) {
            log.warn("Falha ao incrementar cliques no Redis para shortCode: {}. Erro: {}", shortCode, ex.getMessage());
            return 0L;
        }
    }

    @Override
    public long getClicks(String shortCode) {
        try {
            String countStr = redisTemplate.opsForValue().get(CLICKS_KEY_PREFIX + shortCode);
            return countStr != null ? Long.parseLong(countStr) : 0L;
        } catch (Exception ex) {
            log.warn("Falha ao obter cliques no Redis para shortCode: {}. Erro: {}", shortCode, ex.getMessage());
            return 0L;
        }
    }
}
