package com.klezovich.urlshortener.service;

import com.klezovich.urlshortener.domain.FullUrl;
import com.klezovich.urlshortener.domain.ShortenedUrl;
import com.klezovich.urlshortener.domain.ShortenedUrlAccessStats;
import com.klezovich.urlshortener.repository.FullUrlRepository;
import com.klezovich.urlshortener.repository.ShortenedUrlRepository;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UrlShorteningService {

    private static final String[] SUPPORTED_SCHEMES = {"http", "https"};

    private final FullUrlRepository fullUrlRepository;
    private final ShortenedUrlRepository shortenedUrlRepository;

    @Value("${app.domain}")
    private String domain;

    @Value("${app.port}")
    private String port;

    @Autowired
    public UrlShorteningService(FullUrlRepository fullUrlRepository, ShortenedUrlRepository shortenedUrlRepository) {
        this.fullUrlRepository = fullUrlRepository;
        this.shortenedUrlRepository = shortenedUrlRepository;
    }

    public ShortenedUrl shortenUrlForUser(String url, String username) {

        url = normalizeUrl(url);
        validateUrl(url);

        var fullUrlOpt = fullUrlRepository.findByFullUrl(url);
        
        if(fullUrlOpt.isPresent()) {
            return shortenExistingFullUrlForUser(fullUrlOpt.get(), username);
        }

        return shortenNewFullUrlForUser(url, username);
    }

    public FullUrl getFullUrlByShortenedUrlKey(String key, String username) {
        var shortenedUrlOpt = shortenedUrlRepository.findByShortenedUrlKey(key);
        if(!shortenedUrlOpt.isPresent()) {
            throw new RuntimeException("Unknown shortened URL");
        }

        var shortenedUrl = shortenedUrlOpt.get();
        incrementVisitCount(shortenedUrl, username);

        shortenedUrlRepository.save(shortenedUrl);
        return shortenedUrl.getFullUrl();
    }

    void incrementVisitCount(ShortenedUrl shortenedUrl, String username) {
        var accessStatsForUsernameOpt = shortenedUrl.getAccessStats().stream()
                .filter( stats -> stats.getVisitorName().equals(username))
                .findAny();

        if(accessStatsForUsernameOpt.isPresent()) {
            var accessStatsForUsername = accessStatsForUsernameOpt.get();
            accessStatsForUsername.incrementVisitCount();
        } else {
            var accessStats = ShortenedUrlAccessStats.builder()
                    .visitorName(username)
                    .visitCount(1)
                    .shortenedUrl(shortenedUrl)
                    .build();

            shortenedUrl.getAccessStats().add(accessStats);
        }
    }

    ShortenedUrl shortenExistingFullUrlForUser(FullUrl fullUrl, String username) {
        fullUrl.incrementTimesShortened();

        var shortenedUrlForUserOpt = findShortenedUrlForUser(fullUrl, username);

        if(shortenedUrlForUserOpt.isPresent()) {
            fullUrlRepository.save(fullUrl);
            return shortenedUrlForUserOpt.get();
        } else {
            return shortenExistingFullUrlForNewUser(fullUrl, username);
        }
    }

    Optional<ShortenedUrl> findShortenedUrlForUser(FullUrl url, String username) {
        var shortenedUrls = url.getShortenedUrls();
        return shortenedUrls.stream()
                .filter( su -> su.getOwnerName().equals(username))
                .findAny();
    }

    ShortenedUrl shortenExistingFullUrlForNewUser(FullUrl fullUrl, String username) {
        var shortenedUrl = createNewShortenedUrl(fullUrl, username);
        fullUrl.getShortenedUrls().add(shortenedUrl);
        fullUrlRepository.save(fullUrl);

        return shortenedUrl;
    }

    ShortenedUrl shortenNewFullUrlForUser(String url, String username) {
        var fullUrl = createNewFullUrl(url);
        fullUrl= fullUrlRepository.save(fullUrl);

        var shortenedUrl = createNewShortenedUrl(fullUrl, username);
        fullUrl.getShortenedUrls().add(shortenedUrl);
        fullUrlRepository.save(fullUrl);

        return shortenedUrl;
    }
    
    ShortenedUrl createNewShortenedUrl(FullUrl url, String username) {
        var shortenedUrl = ShortenedUrl.builder()
                .fullUrl(url)
                .ownerName(username)
                .accessStats(new ArrayList<>())
                .build();

        shortenedUrl = shortenedUrlRepository.save(shortenedUrl);
        var id = shortenedUrl.getId();
        var urlKey = generateShortenedUrlKey(id);
        shortenedUrl.setShortenedUrlKey(urlKey);

        var shortenedUrlLink = generateShortenedUrlByKey(urlKey);

        shortenedUrl.setShortenedUrl(shortenedUrlLink);

        return shortenedUrl;
    }
    
    FullUrl createNewFullUrl(String url) {
        var fullUlr = FullUrl.builder()
                .fullUrl(url)
                .timesShortened(1)
                .shortenedUrls(new ArrayList<>())
                .build();

        return fullUlr;
    }

    void validateUrl(String fullUrl) {
        var urlValidator = new UrlValidator(SUPPORTED_SCHEMES);
        if(!urlValidator.isValid(fullUrl)) {
            throw new RuntimeException("Invalid URL");
        }
    }

    String normalizeUrl(String fullUrl) {
        for(var scheme : SUPPORTED_SCHEMES ) {
            if(fullUrl.startsWith(scheme+"://")) {
                return fullUrl;
            }
        }

        if(fullUrl.startsWith("www.")) {
            return "http://"+fullUrl;
        }

        return fullUrl;
    }


    String generateShortenedUrlKey( long shortenedUrlId) {
        return idToShortURL(shortenedUrlId);
    }

    String generateShortenedUrlByKey(String key) {
        return "http://"+"www."+ domain +":"+port+"/e/"+key;
    }


    String idToShortURL(long n) {
        // Map to store 62 possible characters
        char map[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

        StringBuffer shorturl = new StringBuffer();

        // Convert given integer id to a base 62 number
        while (n > 0) {
            // use above map to store actual character
            // in short url
            int idx = BigInteger.valueOf(n).mod(BigInteger.valueOf(62)).intValue();
            shorturl.append(map[idx]);
            n = n / 62;
        }

        // Reverse shortURL to complete base conversion
        return shorturl.reverse().toString();
    }
}
