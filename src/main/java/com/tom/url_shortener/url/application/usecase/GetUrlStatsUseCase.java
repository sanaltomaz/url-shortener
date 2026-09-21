package com.tom.url_shortener.url.application.usecase;

import com.tom.url_shortener.url.application.dto.UrlStatsResponse;
import com.tom.url_shortener.url.domain.UrlMetricsRepository;
import com.tom.url_shortener.url.domain.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUrlStatsUseCase {

    private final UrlRepository urlRepository;
    private final UrlMetricsRepository urlMetricsRepository;

    @Transactional(readOnly = true)
    public Optional<UrlStatsResponse> execute(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(url -> {
                    long redisClicks = urlMetricsRepository.getClicks(shortCode);
                    long dbClicks = url.getClickCount() != null ? url.getClickCount() : 0L;
                    long totalClicks = Math.max(dbClicks, redisClicks);

                    return UrlStatsResponse.builder()
                            .originalUrl(url.getOriginalUrl())
                            .shortCode(url.getShortCode())
                            .totalClicks(totalClicks)
                            .createdAt(url.getCreatedAt())
                            .build();
                });
    }
}
