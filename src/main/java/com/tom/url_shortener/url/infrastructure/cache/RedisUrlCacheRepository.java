package com.tom.url_shortener.url.infrastructure.cache;

import com.tom.url_shortener.url.domain.UrlCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class RedisUrlCacheRepository implements UrlCacheRepository {

    private static final String KEY_PREFIX = "url:";

    private final RedisTemplate<String, String> redisTemplate;
    private final Duration ttl;

    public RedisUrlCacheRepository(
            RedisTemplate<String, String> redisTemplate,
            @Value("${app.cache.redis.ttl:24h}") Duration ttl
    ) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }

    @Override
    public Optional<String> findOriginalUrlByShortCode(String shortCode) {
        try {
            String originalUrl = redisTemplate.opsForValue().get(buildKey(shortCode));
            return Optional.ofNullable(originalUrl);
        } catch (Exception ex) {
            log.warn("Falha ao consultar cache Redis para shortCode: {}. Seguindo para o banco de dados. Erro: {}", shortCode, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void save(String shortCode, String originalUrl) {
        try {
            redisTemplate.opsForValue().set(buildKey(shortCode), originalUrl, ttl);
        } catch (Exception ex) {
            log.warn("Falha ao salvar no cache Redis para shortCode: {}. Erro: {}", shortCode, ex.getMessage());
        }
    }

    private String buildKey(String shortCode) {
        return KEY_PREFIX + shortCode;
    }
}
