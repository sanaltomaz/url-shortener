package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOriginalUrlUseCaseTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private GetOriginalUrlUseCase getOriginalUrlUseCase;

    @Test
    @DisplayName("Deve retornar a URL original quando o shortCode for encontrado")
    void shouldReturnOriginalUrlWhenShortCodeExists() {
        String shortCode = "abc12";
        String expectedUrl = "https://spring.io";
        Url url = Url.builder()
                .id(1L)
                .originalUrl(expectedUrl)
                .shortCode(shortCode)
                .build();

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));

        Optional<String> result = getOriginalUrlUseCase.execute(shortCode);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedUrl);

        verify(urlRepository).findByShortCode(shortCode);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o shortCode nao for encontrado")
    void shouldReturnEmptyOptionalWhenShortCodeDoesNotExist() {
        String shortCode = "nonexistent";

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        Optional<String> result = getOriginalUrlUseCase.execute(shortCode);

        assertThat(result).isEmpty();

        verify(urlRepository).findByShortCode(shortCode);
    }
}
