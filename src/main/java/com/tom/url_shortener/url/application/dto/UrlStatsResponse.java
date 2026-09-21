package com.tom.url_shortener.url.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlStatsResponse {
    private String originalUrl;
    private String shortCode;
    private Long totalClicks;
    private Instant createdAt;
}
