package com.tom.url_shortener.url;

import com.tom.url_shortener.url.application.dto.ShortenUrlCommand;
import com.tom.url_shortener.url.application.dto.ShortenUrlResponse;
import com.tom.url_shortener.url.application.usecase.GetOriginalUrlUseCase;
import com.tom.url_shortener.url.application.usecase.ShortenUrlUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ShortenUrlIntegrationTest {

    @Autowired
    private ShortenUrlUseCase shortenUrlUseCase;

    @Autowired
    private GetOriginalUrlUseCase getOriginalUrlUseCase;

    @Test
    @DisplayName("Deve encurtar URL com TSID gerado e persistir com sucesso no banco de dados")
    void shouldShortenAndRetrieveUrlWithTsid() {
        String originalUrl = "https://spring.io/projects/spring-boot";
        ShortenUrlCommand command = new ShortenUrlCommand(originalUrl);

        ShortenUrlResponse response = shortenUrlUseCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isPositive();
        assertThat(response.getOriginalUrl()).isEqualTo(originalUrl);
        assertThat(response.getShortCode()).isNotBlank();

        // Busca pela URL encurtada usando o código gerado
        Optional<String> retrievedOriginalUrl = getOriginalUrlUseCase.execute(response.getShortCode());
        assertThat(retrievedOriginalUrl).contains(originalUrl);

        // Idempotência ao encurtar a mesma URL
        ShortenUrlResponse secondResponse = shortenUrlUseCase.execute(command);
        assertThat(secondResponse.getId()).isEqualTo(response.getId());
        assertThat(secondResponse.getShortCode()).isEqualTo(response.getShortCode());
    }
}
