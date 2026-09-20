package com.tom.url_shortener.url;

import com.tom.url_shortener.url.application.dto.ShortenUrlCommand;
import com.tom.url_shortener.url.application.dto.ShortenUrlResponse;
import com.tom.url_shortener.url.application.usecase.GetOriginalUrlUseCase;
import com.tom.url_shortener.url.application.usecase.ShortenUrlUseCase;
import com.tom.url_shortener.url.infrastructure.persistence.repository.SpringDataUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShortenUrlIntegrationTest {

    @Autowired
    private ShortenUrlUseCase shortenUrlUseCase;

    @Autowired
    private GetOriginalUrlUseCase getOriginalUrlUseCase;

    @Autowired
    private SpringDataUrlRepository springDataUrlRepository;

    @BeforeEach
    void setUp() {
        springDataUrlRepository.deleteAll();
    }

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

    @Test
    @DisplayName("Deve lidar com requisicoes concorrentes para a mesma URL sem falhas de integridade")
    void shouldHandleConcurrentRequestsForSameUrlSafely() throws InterruptedException, ExecutionException {
        String originalUrl = "https://concurrent-test.com";
        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<ShortenUrlResponse>> futures = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                futures.add(executorService.submit(() -> {
                    startLatch.await();
                    return shortenUrlUseCase.execute(new ShortenUrlCommand(originalUrl));
                }));
            }

            startLatch.countDown(); // Libera todas as threads simultaneamente

            Set<String> shortCodes = new HashSet<>();
            for (Future<ShortenUrlResponse> future : futures) {
                ShortenUrlResponse response = future.get();
                assertThat(response).isNotNull();
                assertThat(response.getOriginalUrl()).isEqualTo(originalUrl);
                shortCodes.add(response.getShortCode());
            }

            // Todas as threads concorrentes devem ter convergido para exatamente o mesmo shortCode
            assertThat(shortCodes).hasSize(1);
            assertThat(springDataUrlRepository.count()).isEqualTo(1);

            var persisted = springDataUrlRepository.findByOriginalUrl(originalUrl);
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getShortCode()).isEqualTo(shortCodes.iterator().next());
        }
    }
}
