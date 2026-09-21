package com.tom.url_shortener.url.infrastructure.persistence.repository;

import com.tom.url_shortener.url.infrastructure.persistence.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUrlRepository extends JpaRepository<UrlEntity, Long> {

    Optional<UrlEntity> findByShortCode(String shortCode);

    Optional<UrlEntity> findByOriginalUrl(String originalUrl);

    @Modifying
    @Query("UPDATE UrlEntity u SET u.clickCount = :clickCount WHERE u.shortCode = :shortCode")
    void updateClickCount(@Param("shortCode") String shortCode, @Param("clickCount") Long clickCount);
}
