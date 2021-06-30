package com.klezovich.urlshortener.repository;

import com.klezovich.urlshortener.domain.FullUrl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FullUrlRepository extends CrudRepository<FullUrl, Long> {

    Optional<FullUrl> findByFullUrl(String fullUrl);
}
