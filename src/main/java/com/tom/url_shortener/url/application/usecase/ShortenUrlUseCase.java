package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.application.dto.ShortenUrlCommand;
import com.tom.url_shortener.url.application.dto.ShortenUrlResponse;
import com.tom.url_shortener.url.domain.IdGenerator;
import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlRepository;
import com.tom.url_shortener.url.infrastructure.utils.Base62;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShortenUrlUseCase {

    private final UrlRepository urlRepository;
    private final IdGenerator idGenerator;

    @Transactional
    public ShortenUrlResponse execute(ShortenUrlCommand command) {
        String originalUrl = command.getOriginalUrl();

        // Se a URL já foi encurtada previamente, podemos retornar a existente
        return urlRepository.findByOriginalUrl(originalUrl)
                .map(this::toResponse)
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

        Url savedUrl = urlRepository.save(url);

        return toResponse(savedUrl);
    }

    private ShortenUrlResponse toResponse(Url url) {
        return ShortenUrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .build();
    }
}
