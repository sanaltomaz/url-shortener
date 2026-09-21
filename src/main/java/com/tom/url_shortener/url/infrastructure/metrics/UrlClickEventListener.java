package com.tom.url_shortener.url.infrastructure.metrics;

import com.tom.url_shortener.url.domain.UrlMetricsRepository;
import com.tom.url_shortener.url.domain.event.UrlClickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlClickEventListener {

    private final UrlMetricsRepository urlMetricsRepository;

    @Async
    @EventListener
    public void handleUrlClicked(UrlClickedEvent event) {
        log.debug("Processando métrica de clique assíncrona para shortCode: {}", event.shortCode());
        urlMetricsRepository.incrementClicks(event.shortCode());
    }
}
