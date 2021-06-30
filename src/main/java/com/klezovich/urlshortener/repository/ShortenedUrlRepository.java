package com.klezovich.urlshortener.repository;

import com.klezovich.urlshortener.domain.ShortenedUrl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortenedUrlRepository extends CrudRepository<ShortenedUrl, Long> {

    Optional<ShortenedUrl> findByShortenedUrlKey(String shortenedUrlKey);
}
