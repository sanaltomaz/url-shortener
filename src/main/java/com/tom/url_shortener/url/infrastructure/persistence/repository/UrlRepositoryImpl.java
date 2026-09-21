package com.tom.url_shortener.url.infrastructure.persistence.repository;

import com.tom.url_shortener.url.domain.Url;
import com.tom.url_shortener.url.domain.UrlRepository;
import com.tom.url_shortener.url.infrastructure.persistence.entity.UrlEntity;
import com.tom.url_shortener.url.infrastructure.persistence.mapper.UrlEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UrlRepositoryImpl implements UrlRepository {

    private final SpringDataUrlRepository springDataUrlRepository;
    private final UrlEntityMapper urlEntityMapper;

    @Override
    public Url save(Url url) {
        UrlEntity entity = urlEntityMapper.toEntity(url);
        UrlEntity savedEntity = springDataUrlRepository.save(entity);
        return urlEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Url> findById(Long id) {
        return springDataUrlRepository.findById(id)
                .map(urlEntityMapper::toDomain);
    }

    @Override
    public Optional<Url> findByShortCode(String shortCode) {
        return springDataUrlRepository.findByShortCode(shortCode)
                .map(urlEntityMapper::toDomain);
    }

    @Override
    public Optional<Url> findByOriginalUrl(String originalUrl) {
        return springDataUrlRepository.findByOriginalUrl(originalUrl)
                .map(urlEntityMapper::toDomain);
    }

    @Override
    @Transactional
    public void updateClickCount(String shortCode, long clickCount) {
        springDataUrlRepository.updateClickCount(shortCode, clickCount);
    }
}
