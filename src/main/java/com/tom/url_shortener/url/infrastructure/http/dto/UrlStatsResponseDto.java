package com.tom.url_shortener.url.infrastructure.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlStatsResponseDto {
    private String originalUrl;
    private String shortCode;
    private Long totalClicks;
    private Instant createdAt;
}
