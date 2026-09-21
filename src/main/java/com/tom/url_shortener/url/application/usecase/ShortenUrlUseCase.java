package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.application.dto.ShortenUrlCommand;
import com.tom.url_shortener.url.application.dto.ShortenUrlResponse;
import com.tom.url_shortener.url.domain.IdGenerator;
import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlCacheRepository;
import com.tom.url_shortener.url.domain.UrlRepository;
import com.tom.url_shortener.url.infrastructure.utils.Base62;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortenUrlUseCase {

    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final IdGenerator idGenerator;

    public ShortenUrlResponse execute(ShortenUrlCommand command) {
        String originalUrl = command.getOriginalUrl();

        // Se a URL já foi encurtada previamente, aquecemos o cache e retornamos a existente
        return urlRepository.findByOriginalUrl(originalUrl)
                .map(existingUrl -> {
                    urlCacheRepository.save(existingUrl.getShortCode(), existingUrl.getOriginalUrl());
                    return toResponse(existingUrl);
                })
                .orElseGet(() -> createNewUrl(originalUrl));
    }

    private ShortenUrlResponse createNewUrl(String originalUrl) {
        long id = idGenerator.nextId();
        String shortCode = Base62.encode(id);

        Url url = Url.builder()
                .id(id)
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .build();

        try {
            Url savedUrl = urlRepository.save(url);
            // Aquecimento preventivo de cache na escrita
            urlCacheRepository.save(savedUrl.getShortCode(), savedUrl.getOriginalUrl());
            return toResponse(savedUrl);
        } catch (DataIntegrityViolationException ex) {
            // Em caso de concorrência onde outra requisição inseriu a mesma URL em paralelo,
            // recuperamos a URL já persistida garantindo a idempotência e aquecemos o cache.
            log.info("Concorrência detectada para a URL: {}. Recuperando registro existente.", originalUrl);
            return urlRepository.findByOriginalUrl(originalUrl)
                    .map(existingUrl -> {
                        urlCacheRepository.save(existingUrl.getShortCode(), existingUrl.getOriginalUrl());
                        return toResponse(existingUrl);
                    })
                    .orElseThrow(() -> ex);
        }
    }

    private ShortenUrlResponse toResponse(Url url) {
        return ShortenUrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .build();
    }
}
