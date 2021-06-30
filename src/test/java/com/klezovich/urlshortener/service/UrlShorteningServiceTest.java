package com.klezovich.urlshortener.service;

import com.klezovich.urlshortener.repository.FullUrlRepository;
import com.klezovich.urlshortener.repository.ShortenedUrlRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration_test")
class UrlShorteningServiceTest {

    @Autowired
    private UrlShorteningService service;

    @Autowired
    private FullUrlRepository fullUrlRepository;

    @Autowired
    private ShortenedUrlRepository shortenedUrlRepository;

    @Test
    void testUrlNormalisation() {
        var url = "http://www.google.com";
        assertEquals(url, service.normalizeUrl(url));

        url = "https://www.google.com";
        assertEquals(url, service.normalizeUrl(url));

        url = "www.google.com";
        assertEquals("http://" + url, service.normalizeUrl(url));

        url = "ftp://abc.com";
        assertEquals(url, service.normalizeUrl(url));
    }

    @Test
    void testUrlValidation() {
        service.validateUrl("http://www.google.com");
        service.validateUrl("https://www.google.com");

        assertThrows(RuntimeException.class, () -> service.validateUrl("www.google.com"));
        assertThrows(RuntimeException.class, () -> service.validateUrl("ftp://www.google.com"));
        assertThrows(RuntimeException.class, () -> service.validateUrl("notavalidurl"));
    }

    @Test
    @Transactional
    void testCanCorrectlyShortenNewFullUrl() {
        service.shortenUrlForUser("www.google.com", "spring");

        var fullUrls = toList(fullUrlRepository.findAll());
        assertEquals(1, fullUrls.size());

        var shortenedUrls = fullUrls.get(0).getShortenedUrls();
        assertEquals(1, shortenedUrls.size());

        var shortenedUrl = shortenedUrls.get(0);
        assertNotNull(shortenedUrl.getShortenedUrl());
        assertNotNull(shortenedUrl.getShortenedUrlKey());
        assertEquals("spring", shortenedUrl.getOwnerName());
    }

    @Test
    @Transactional
    void testCanCorrectlyShortenExistingFullUrlForNewUser() {
        service.shortenUrlForUser("www.google.com", "spring");
        service.shortenUrlForUser("www.google.com", "admin");
        service.shortenUrlForUser("www.google.com", "anonymous");
        service.shortenUrlForUser("www.google.com", "anonymous");


        var fullUrls = toList(fullUrlRepository.findAll());
        assertEquals(1, fullUrls.size());

        var shortenedUrls = fullUrls.get(0).getShortenedUrls();
        assertEquals(3, shortenedUrls.size());
    }

    @Test
    @Transactional
    void testShortenedUrlAccessByNewUserCreatesNewAccessStatistic() {
        var shortenedUrl = service.shortenUrlForUser("www.google.com", "spring");

        var key = shortenedUrl.getShortenedUrlKey();
        var id = shortenedUrl.getId();

        service.getFullUrlByShortenedUrlKey(key, "spring");

        var shortenedUrlFromDb = shortenedUrlRepository.findByShortenedUrlKey(key).get();
        var accessStats = shortenedUrlFromDb.getAccessStats();

        assertEquals(1, accessStats.size());

        var accessStat = accessStats.get(0);
        assertEquals(1, accessStat.getVisitCount());
        assertEquals("spring", accessStat.getVisitorName());
    }

    @Test
    @Transactional
    void testShortenedUrlAccessByAlreadyVisitedUserIncrementsAccessCount() {
        var shortenedUrl = service.shortenUrlForUser("www.google.com", "spring");

        var key = shortenedUrl.getShortenedUrlKey();
        var id = shortenedUrl.getId();

        service.getFullUrlByShortenedUrlKey(key, "spring");
        service.getFullUrlByShortenedUrlKey(key, "spring");

        var shortenedUrlFromDb = shortenedUrlRepository.findByShortenedUrlKey(key).get();
        var accessStats = shortenedUrlFromDb.getAccessStats();

        assertEquals(1, accessStats.size());

        var accessStat = accessStats.get(0);
        assertEquals(2, accessStat.getVisitCount());
        assertEquals("spring", accessStat.getVisitorName());
    }

    @Test
    @Transactional
    void testAccessByDifferentUsersCreatesDifferentAccessStatistics() {
        var shortenedUrl = service.shortenUrlForUser("www.google.com", "spring");

        var key = shortenedUrl.getShortenedUrlKey();
        var id = shortenedUrl.getId();

        service.getFullUrlByShortenedUrlKey(key, "spring");
        service.getFullUrlByShortenedUrlKey(key, "admin");

        var shortenedUrlFromDb = shortenedUrlRepository.findByShortenedUrlKey(key).get();
        var accessStats = shortenedUrlFromDb.getAccessStats();

        assertEquals(2, accessStats.size());
    }

    <T> List<T> toList(Iterable<T> urls) {
        return StreamSupport.stream(urls.spliterator(), false)
                .collect(Collectors.toList());
    }

}