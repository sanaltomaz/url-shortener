package com.tom.url_shortener.url.domain;

public interface UrlMetricsRepository {

    long incrementClicks(String shortCode);

    long getClicks(String shortCode);
}
