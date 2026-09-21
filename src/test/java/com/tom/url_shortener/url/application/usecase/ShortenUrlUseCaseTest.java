package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.application.dto.ShortenUrlCommand;
import com.tom.url_shortener.url.application.dto.ShortenUrlResponse;
import com.tom.url_shortener.url.domain.IdGenerator;
import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlCacheRepository;
import com.tom.url_shortener.url.domain.UrlRepository;
import com.tom.url_shortener.url.infrastructure.utils.Base62;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortenUrlUseCaseTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCacheRepository urlCacheRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private ShortenUrlUseCase shortenUrlUseCase;

    @Test
    @DisplayName("Deve retornar URL existente e aquecer cache quando a URL original ja estiver cadastrada")
    void shouldReturnExistingUrlWhenAlreadyExists() {
        String originalUrl = "https://google.com";
        ShortenUrlCommand command = new ShortenUrlCommand(originalUrl);
        Url existingUrl = Url.builder()
                .id(10L)
                .originalUrl(originalUrl)
                .shortCode("a")
                .build();

        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existingUrl));

        ShortenUrlResponse response = shortenUrlUseCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getOriginalUrl()).isEqualTo(originalUrl);
        assertThat(response.getShortCode()).isEqualTo("a");

        verify(urlRepository).findByOriginalUrl(originalUrl);
        verify(urlCacheRepository).save("a", originalUrl);
        verify(urlRepository, never()).save(any());
        verifyNoInteractions(idGenerator);
    }

    @Test
    @DisplayName("Deve gerar ID e shortCode pre-persistencia, salvar no banco e aquecer cache")
    void shouldCreateAndReturnNewUrlWhenNotExists() {
        String originalUrl = "https://github.com";
        ShortenUrlCommand command = new ShortenUrlCommand(originalUrl);
        long generatedId = 62L;
        String expectedCode = Base62.encode(generatedId);

        Url savedUrl = Url.builder()
                .id(generatedId)
                .originalUrl(originalUrl)
                .shortCode(expectedCode)
                .build();

        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn(generatedId);
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        ShortenUrlResponse response = shortenUrlUseCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(generatedId);
        assertThat(response.getOriginalUrl()).isEqualTo(originalUrl);
        assertThat(response.getShortCode()).isEqualTo(expectedCode);

        verify(urlRepository).findByOriginalUrl(originalUrl);
        verify(idGenerator).nextId();
        verify(urlRepository, times(1)).save(argThat(url ->
                url.getId().equals(generatedId) &&
                url.getOriginalUrl().equals(originalUrl) &&
                url.getShortCode().equals(expectedCode)
        ));
        verify(urlCacheRepository).save(expectedCode, originalUrl);
    }

    @Test
    @DisplayName("Deve recuperar registro existente e aquecer cache quando ocorrer DataIntegrityViolationException")
    void shouldRecoverExistingUrlWhenDataIntegrityViolationOccursDueToRaceCondition() {
        String originalUrl = "https://concurrent.com";
        ShortenUrlCommand command = new ShortenUrlCommand(originalUrl);
        long generatedId = 99L;

        Url existingUrlSavedByOtherThread = Url.builder()
                .id(88L)
                .originalUrl(originalUrl)
                .shortCode("otherThreadCode")
                .build();

        // 1ª busca: não encontra nada
        // 2ª busca (após conflito de chave única): encontra a URL que outra thread salvou
        when(urlRepository.findByOriginalUrl(originalUrl))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingUrlSavedByOtherThread));

        when(idGenerator.nextId()).thenReturn(generatedId);
        when(urlRepository.save(any(Url.class))).thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        ShortenUrlResponse response = shortenUrlUseCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(88L);
        assertThat(response.getOriginalUrl()).isEqualTo(originalUrl);
        assertThat(response.getShortCode()).isEqualTo("otherThreadCode");

        verify(urlRepository, times(2)).findByOriginalUrl(originalUrl);
        verify(urlRepository, times(1)).save(any(Url.class));
        verify(urlCacheRepository).save("otherThreadCode", originalUrl);
    }
}
