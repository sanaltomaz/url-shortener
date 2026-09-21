package com.tom.url_shortener.url.infrastructure.metrics;

import com.tom.url_shortener.url.domain.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClickMetricsSyncSchedulerTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ClickMetricsSyncScheduler scheduler;

    @Test
    @DisplayName("Deve iterar sobre as chaves do Redis e sincronizar a contagem no banco")
    void shouldSyncClicksToDatabase() {
        Set<String> keys = Set.of("url:clicks:codeA", "url:clicks:codeB");
        when(redisTemplate.keys("url:clicks:*")).thenReturn(keys);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:clicks:codeA")).thenReturn("12");
        when(valueOperations.get("url:clicks:codeB")).thenReturn("55");

        scheduler.syncClicksToDatabase();

        verify(urlRepository).updateClickCount("codeA", 12L);
        verify(urlRepository).updateClickCount("codeB", 55L);
    }

    @Test
    @DisplayName("Nao deve sincronizar se nenhuma chave existir no Redis")
    void shouldDoNothingWhenNoKeysExist() {
        when(redisTemplate.keys("url:clicks:*")).thenReturn(Set.of());

        scheduler.syncClicksToDatabase();

        verifyNoInteractions(urlRepository);
    }
}
