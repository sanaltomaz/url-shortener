package com.tom.url_shortener.url.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {
    private Long id;
    private String originalUrl;
    private String shortCode;
}
