package com.tom.url_shortener.url.infrastructure.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisUrlCacheRepositoryTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisUrlCacheRepository redisUrlCacheRepository;
    private final Duration ttl = Duration.ofHours(24);

    @BeforeEach
    void setUp() {
        redisUrlCacheRepository = new RedisUrlCacheRepository(redisTemplate, ttl);
    }

    @Test
    @DisplayName("Deve retornar a URL original quando o shortCode existir no cache Redis")
    void shouldReturnOriginalUrlWhenKeyExists() {
        String shortCode = "abc1234567";
        String expectedUrl = "https://spring.io";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:" + shortCode)).thenReturn(expectedUrl);

        Optional<String> result = redisUrlCacheRepository.findOriginalUrlByShortCode(shortCode);

        assertThat(result).isPresent().contains(expectedUrl);
        verify(valueOperations).get("url:" + shortCode);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o shortCode nao existir no cache")
    void shouldReturnEmptyOptionalWhenKeyDoesNotExist() {
        String shortCode = "not-found";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:" + shortCode)).thenReturn(null);

        Optional<String> result = redisUrlCacheRepository.findOriginalUrlByShortCode(shortCode);

        assertThat(result).isEmpty();
        verify(valueOperations).get("url:" + shortCode);
    }

    @Test
    @DisplayName("Deve capturar excecao do Redis e retornar Optional vazio de forma resiliente")
    void shouldReturnEmptyOptionalResilientlyWhenRedisFailsOnGet() {
        String shortCode = "abc1234567";
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        Optional<String> result = redisUrlCacheRepository.findOriginalUrlByShortCode(shortCode);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve salvar o par shortCode:originalUrl com prefixo e TTL configurado")
    void shouldSaveWithPrefixAndTtl() {
        String shortCode = "abc1234567";
        String originalUrl = "https://spring.io";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisUrlCacheRepository.save(shortCode, originalUrl);

        verify(valueOperations).set("url:" + shortCode, originalUrl, ttl);
    }

    @Test
    @DisplayName("Deve capturar excecao do Redis ao salvar sem quebrar a aplicacao")
    void shouldHandleExceptionGracefullyWhenRedisFailsOnSave() {
        String shortCode = "abc1234567";
        String originalUrl = "https://spring.io";
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        // Nao deve lancar excecao
        redisUrlCacheRepository.save(shortCode, originalUrl);
    }
}
