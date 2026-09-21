package com.tom.url_shortener.url.infrastructure.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisUrlMetricsRepositoryTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisUrlMetricsRepository metricsRepository;

    @BeforeEach
    void setUp() {
        metricsRepository = new RedisUrlMetricsRepository(redisTemplate);
    }

    @Test
    @DisplayName("Deve incrementar cliques atomicamente no Redis")
    void shouldIncrementClicksAtomically() {
        String shortCode = "test123456";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("url:clicks:" + shortCode)).thenReturn(5L);

        long clicks = metricsRepository.incrementClicks(shortCode);

        assertThat(clicks).isEqualTo(5L);
        verify(valueOperations).increment("url:clicks:" + shortCode);
    }

    @Test
    @DisplayName("Deve retornar 0L de forma resiliente ao falhar o incremento no Redis")
    void shouldHandleExceptionGracefullyOnIncrement() {
        String shortCode = "test123456";
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        long clicks = metricsRepository.incrementClicks(shortCode);

        assertThat(clicks).isEqualTo(0L);
    }

    @Test
    @DisplayName("Deve obter contador de cliques do Redis quando a chave existir")
    void shouldGetClicksWhenKeyExists() {
        String shortCode = "test123456";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:clicks:" + shortCode)).thenReturn("15");

        long clicks = metricsRepository.getClicks(shortCode);

        assertThat(clicks).isEqualTo(15L);
        verify(valueOperations).get("url:clicks:" + shortCode);
    }

    @Test
    @DisplayName("Deve retornar 0L quando a chave nao existir no Redis")
    void shouldReturnZeroWhenKeyDoesNotExist() {
        String shortCode = "notfound";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:clicks:" + shortCode)).thenReturn(null);

        long clicks = metricsRepository.getClicks(shortCode);

        assertThat(clicks).isEqualTo(0L);
        verify(valueOperations).get("url:clicks:" + shortCode);
    }

    @Test
    @DisplayName("Deve retornar 0L de forma resiliente ao falhar a leitura no Redis")
    void shouldHandleExceptionGracefullyOnGetClicks() {
        String shortCode = "test123456";
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        long clicks = metricsRepository.getClicks(shortCode);

        assertThat(clicks).isEqualTo(0L);
    }
}
