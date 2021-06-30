package com.klezovich.urlshortener.repository;

import com.klezovich.urlshortener.domain.FullUrl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FullUrlRepositoryTest {

    @Autowired
    private FullUrlRepository repository;

    @Test
    void testCanSaveAndLoadFullUrl() {
        var fullUrl = FullUrl.builder()
                .fullUrl("http://www.google.com")
                .shortenedUrls(new ArrayList<>())
                .timesShortened(1)
                .build();

        var id = repository.save(fullUrl).getId();

        var fullUrlFromDb = repository.findById(id).get();

        assertEquals("http://www.google.com",fullUrlFromDb.getFullUrl());
        assertEquals(1,fullUrlFromDb.getTimesShortened());
    }
}