package com.tom.url_shortener.url.domain;

import java.util.Optional;

public interface UrlRepository {
    Url save(Url url);
    Optional<Url> findById(Long id);
    Optional<Url> findByShortCode(String shortCode);
    Optional<Url> findByOriginalUrl(String originalUrl);
    void updateClickCount(String shortCode, long clickCount);
}
