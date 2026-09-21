package com.tom.url_shortener.url.domain;

import java.util.Optional;

public interface UrlCacheRepository {

    Optional<String> findOriginalUrlByShortCode(String shortCode);

    void save(String shortCode, String originalUrl);
}
