package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetOriginalUrlUseCase {

    private final UrlRepository urlRepository;

    @Transactional(readOnly = true)
    public Optional<String> execute(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(Url::getOriginalUrl);
    }
}
