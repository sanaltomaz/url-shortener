package com.tom.url_shortener.url.infrastructure.persistence.repository;

import com.tom.url_shortener.url.infrastructure.persistence.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    Optional<UrlEntity> findByShortCode(String shortCode);
    Optional<UrlEntity> findByOriginalUrl(String originalUrl);
}
