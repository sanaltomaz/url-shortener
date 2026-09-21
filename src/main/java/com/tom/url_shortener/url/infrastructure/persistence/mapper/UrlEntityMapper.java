package com.tom.url_shortener.url.infrastructure.persistence.mapper;

import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.infrastructure.persistence.entity.UrlEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UrlEntityMapper {

    public UrlEntity toEntity(Url domain) {
        if (domain == null) {
            return null;
        }
        return UrlEntity.builder()
                .id(domain.getId())
                .originalUrl(domain.getOriginalUrl())
                .shortCode(domain.getShortCode())
                .clickCount(domain.getClickCount() != null ? domain.getClickCount() : 0L)
                .createdAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now())
                .build();
    }

    public Url toDomain(UrlEntity entity) {
        if (entity == null) {
            return null;
        }
        return Url.builder()
                .id(entity.getId())
                .originalUrl(entity.getOriginalUrl())
                .shortCode(entity.getShortCode())
                .clickCount(entity.getClickCount() != null ? entity.getClickCount() : 0L)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
