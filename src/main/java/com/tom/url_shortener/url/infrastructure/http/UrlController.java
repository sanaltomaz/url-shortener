package com.tom.url_shortener.url.infrastructure.http;

import com.tom.url_shortener.url.application.dto.ShortenUrlCommand;
import com.tom.url_shortener.url.application.dto.ShortenUrlResponse;
import com.tom.url_shortener.url.application.usecase.GetOriginalUrlUseCase;
import com.tom.url_shortener.url.application.usecase.GetUrlStatsUseCase;
import com.tom.url_shortener.url.application.usecase.ShortenUrlUseCase;
import com.tom.url_shortener.url.infrastructure.http.dto.ShortenUrlRequest;
import com.tom.url_shortener.url.infrastructure.http.dto.ShortenUrlResponseDto;
import com.tom.url_shortener.url.infrastructure.http.dto.UrlStatsResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final ShortenUrlUseCase shortenUrlUseCase;
    private final GetOriginalUrlUseCase getOriginalUrlUseCase;
    private final GetUrlStatsUseCase getUrlStatsUseCase;

    @PostMapping({"/api/v1/urls", "/api/urls"})
    public ResponseEntity<ShortenUrlResponseDto> shortenUrl(
            @RequestBody @Valid ShortenUrlRequest request,
            HttpServletRequest servletRequest) {

        ShortenUrlCommand command = new ShortenUrlCommand(request.getUrl());
        ShortenUrlResponse response = shortenUrlUseCase.execute(command);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(servletRequest)
                .replacePath(null)
                .build()
                .toUriString();

        String fullShortUrl = baseUrl + "/" + response.getShortCode();

        ShortenUrlResponseDto responseDto = ShortenUrlResponseDto.builder()
                .originalUrl(response.getOriginalUrl())
                .shortCode(response.getShortCode())
                .shortUrl(fullShortUrl)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return getOriginalUrlUseCase.execute(shortCode)
                .map(originalUrl -> ResponseEntity
                        .status(HttpStatus.FOUND) // HTTP 302 Redirect
                        .location(URI.create(originalUrl))
                        .<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping({"/api/v1/urls/{shortCode}/stats", "/api/urls/{shortCode}/stats"})
    public ResponseEntity<UrlStatsResponseDto> getStats(@PathVariable String shortCode) {
        return getUrlStatsUseCase.execute(shortCode)
                .map(stats -> ResponseEntity.ok(UrlStatsResponseDto.builder()
                        .originalUrl(stats.getOriginalUrl())
                        .shortCode(stats.getShortCode())
                        .totalClicks(stats.getTotalClicks())
                        .createdAt(stats.getCreatedAt())
                        .build()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
