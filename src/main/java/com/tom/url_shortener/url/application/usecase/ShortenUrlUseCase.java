package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.application.dto.ShortenUrlCommand;
import com.tom.url_shortener.url.application.dto.ShortenUrlResponse;
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

    @Transactional
    public ShortenUrlResponse execute(ShortenUrlCommand command) {
        String originalUrl = command.getOriginalUrl();

        // Se a URL já foi encurtada previamente, podemos retornar a existente
        return urlRepository.findByOriginalUrl(originalUrl)
                .map(this::toResponse)
                .orElseGet(() -> createNewUrl(originalUrl));
    }

    private ShortenUrlResponse createNewUrl(String originalUrl) {
        // 1. Salva inicialmente para gerar o ID sequencial no banco
        Url url = Url.builder()
                .originalUrl(originalUrl)
                .build();
        Url savedUrl = urlRepository.save(url);

        // 2. Codifica o ID com Base62 para gerar o shortCode
        String shortCode = Base62.encode(savedUrl.getId());
        savedUrl.setShortCode(shortCode);

        // 3. Persiste a URL atualizada com o shortCode gerado
        Url updatedUrl = urlRepository.save(savedUrl);

        return toResponse(updatedUrl);
    }

    private ShortenUrlResponse toResponse(Url url) {
        return ShortenUrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .build();
    }
}
