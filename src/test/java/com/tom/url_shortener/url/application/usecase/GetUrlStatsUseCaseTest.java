package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.application.dto.UrlStatsResponse;
import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlMetricsRepository;
import com.tom.url_shortener.url.domain.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUrlStatsUseCaseTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlMetricsRepository urlMetricsRepository;

    @InjectMocks
    private GetUrlStatsUseCase getUrlStatsUseCase;

    @Test
    @DisplayName("Deve retornar estatisticas com cliques agregados quando a URL existir")
    void shouldReturnStatsWhenUrlExists() {
        String shortCode = "13IQGjTqdZ9";
        Instant createdAt = Instant.parse("2026-09-20T20:00:00Z");
        Url url = Url.builder()
                .id(1L)
                .originalUrl("https://spring.io")
                .shortCode(shortCode)
                .clickCount(10L)
                .createdAt(createdAt)
                .build();

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));
        when(urlMetricsRepository.getClicks(shortCode)).thenReturn(42L);

        Optional<UrlStatsResponse> result = getUrlStatsUseCase.execute(shortCode);

        assertThat(result).isPresent();
        UrlStatsResponse stats = result.get();
        assertThat(stats.getShortCode()).isEqualTo(shortCode);
        assertThat(stats.getOriginalUrl()).isEqualTo("https://spring.io");
        assertThat(stats.getTotalClicks()).isEqualTo(42L);
        assertThat(stats.getCreatedAt()).isEqualTo(createdAt);

        verify(urlRepository).findByShortCode(shortCode);
        verify(urlMetricsRepository).getClicks(shortCode);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando a URL nao for encontrada")
    void shouldReturnEmptyOptionalWhenUrlDoesNotExist() {
        String shortCode = "nonexistent";

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        Optional<UrlStatsResponse> result = getUrlStatsUseCase.execute(shortCode);

        assertThat(result).isEmpty();

        verify(urlRepository).findByShortCode(shortCode);
        verifyNoInteractions(urlMetricsRepository);
    }
}
