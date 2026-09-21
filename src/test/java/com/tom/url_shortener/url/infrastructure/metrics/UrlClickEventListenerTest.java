package com.tom.url_shortener.url.infrastructure.metrics;

import com.tom.url_shortener.url.domain.UrlMetricsRepository;
import com.tom.url_shortener.url.domain.event.UrlClickedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UrlClickEventListenerTest {

    @Mock
    private UrlMetricsRepository urlMetricsRepository;

    @InjectMocks
    private UrlClickEventListener listener;

    @Test
    @DisplayName("Deve chamar incremento de cliques ao receber UrlClickedEvent")
    void shouldIncrementClicksOnEvent() {
        UrlClickedEvent event = new UrlClickedEvent("short123");

        listener.handleUrlClicked(event);

        verify(urlMetricsRepository).incrementClicks("short123");
    }
}
