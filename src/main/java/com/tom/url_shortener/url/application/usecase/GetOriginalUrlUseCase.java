package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlCacheRepository;
import com.tom.url_shortener.url.domain.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetOriginalUrlUseCase {

    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;

    @Transactional(readOnly = true)
    public Optional<String> execute(String shortCode) {
        // 1. Busca primeiro no cache (Redis)
        Optional<String> cachedUrl = urlCacheRepository.findOriginalUrlByShortCode(shortCode);
        if (cachedUrl.isPresent()) {
            log.debug("Cache hit para shortCode: {}", shortCode);
            return cachedUrl;
        }

        log.debug("Cache miss para shortCode: {}. Buscando no banco de dados.", shortCode);

        // 2. Se houver miss, busca no repositório persistente (Postgres/H2)
        return urlRepository.findByShortCode(shortCode)
                .map(Url::getOriginalUrl)
                .map(originalUrl -> {
                    // 3. Salva no cache com TTL para aquecimento
                    urlCacheRepository.save(shortCode, originalUrl);
                    return originalUrl;
                });
    }
}
