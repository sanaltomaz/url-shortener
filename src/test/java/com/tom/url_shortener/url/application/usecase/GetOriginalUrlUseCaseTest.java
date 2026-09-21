package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlCacheRepository;
import com.tom.url_shortener.url.domain.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOriginalUrlUseCaseTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCacheRepository urlCacheRepository;

    @InjectMocks
    private GetOriginalUrlUseCase getOriginalUrlUseCase;

    @Test
    @DisplayName("Deve retornar a URL original imediatamente do cache quando houver cache HIT sem consultar banco")
    void shouldReturnOriginalUrlFromCacheWhenHit() {
        String shortCode = "abc12";
        String cachedUrl = "https://spring.io";

        when(urlCacheRepository.findOriginalUrlByShortCode(shortCode)).thenReturn(Optional.of(cachedUrl));

        Optional<String> result = getOriginalUrlUseCase.execute(shortCode);

        assertThat(result).isPresent().contains(cachedUrl);

        verify(urlCacheRepository).findOriginalUrlByShortCode(shortCode);
        verifyNoInteractions(urlRepository);
        verify(urlCacheRepository, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve buscar no banco e salvar no cache quando houver cache MISS")
    void shouldFetchFromRepositoryAndSaveInCacheWhenCacheMiss() {
        String shortCode = "abc12";
        String dbUrl = "https://spring.io";
        Url url = Url.builder()
                .id(1L)
                .originalUrl(dbUrl)
                .shortCode(shortCode)
                .build();

        when(urlCacheRepository.findOriginalUrlByShortCode(shortCode)).thenReturn(Optional.empty());
        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));

        Optional<String> result = getOriginalUrlUseCase.execute(shortCode);

        assertThat(result).isPresent().contains(dbUrl);

        verify(urlCacheRepository).findOriginalUrlByShortCode(shortCode);
        verify(urlRepository).findByShortCode(shortCode);
        verify(urlCacheRepository).save(shortCode, dbUrl);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o shortCode nao for encontrado no cache nem no banco")
    void shouldReturnEmptyOptionalWhenNeitherCacheNorRepositoryHasShortCode() {
        String shortCode = "nonexistent";

        when(urlCacheRepository.findOriginalUrlByShortCode(shortCode)).thenReturn(Optional.empty());
        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        Optional<String> result = getOriginalUrlUseCase.execute(shortCode);

        assertThat(result).isEmpty();

        verify(urlCacheRepository).findOriginalUrlByShortCode(shortCode);
        verify(urlRepository).findByShortCode(shortCode);
        verify(urlCacheRepository, never()).save(anyString(), anyString());
    }
}
