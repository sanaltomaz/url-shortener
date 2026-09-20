package com.tom.url_shortener.url.infrastructure.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlResponseDto {
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
}
