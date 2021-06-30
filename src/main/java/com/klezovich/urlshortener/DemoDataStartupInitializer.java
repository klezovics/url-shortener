package com.klezovich.urlshortener;

import com.klezovich.urlshortener.domain.FullUrl;
import com.klezovich.urlshortener.domain.ShortenedUrl;
import com.klezovich.urlshortener.domain.ShortenedUrlAccessStats;
import com.klezovich.urlshortener.repository.FullUrlRepository;
import com.klezovich.urlshortener.repository.ShortenedUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Profile("!integration_test")
public class DemoDataStartupInitializer implements ApplicationRunner {

    @Autowired
    private final FullUrlRepository fullUrlRepository;

    public DemoDataStartupInitializer(FullUrlRepository repository) {
        this.fullUrlRepository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

       var fullUrl = FullUrl.builder()
             .fullUrl("http://www.google.com")
             .timesShortened(2)
             .shortenedUrls(new ArrayList<>())
             .build();

       var shortenedUrl1 = ShortenedUrl.builder()
                .fullUrl(fullUrl)
                .shortenedUrlKey("ABCDE")
                .shortenedUrl("http://localhost:8080/e/ABCDE")
                .ownerName("spring")
                .accessStats(new ArrayList<>())
                .build();

        var shortenedUrl2 = ShortenedUrl.builder()
                .fullUrl(fullUrl)
                .shortenedUrlKey("EDCBA")
                .shortenedUrl("http://localhost:8080/e/EDCBA")
                .ownerName("admin")
                .accessStats(new ArrayList<>())
                .build();

       var shortenedUrlAccessStat1 = ShortenedUrlAccessStats.builder()
               .shortenedUrl(shortenedUrl1)
               .visitCount(1)
               .visitorName("spring")
               .build();

       var shortenedUrlAccessStat2 = ShortenedUrlAccessStats.builder()
               .shortenedUrl(shortenedUrl1)
               .visitCount(2)
               .visitorName("admin")
               .build();

        var shortenedUrlAccessStat3 = ShortenedUrlAccessStats.builder()
                .shortenedUrl(shortenedUrl2)
                .visitCount(100)
                .visitorName("anonymous")
                .build();

       shortenedUrl1.getAccessStats().add(shortenedUrlAccessStat1);
       shortenedUrl1.getAccessStats().add(shortenedUrlAccessStat2);
       shortenedUrl2.getAccessStats().add(shortenedUrlAccessStat3);

       fullUrl.getShortenedUrls().add(shortenedUrl1);
       fullUrl.getShortenedUrls().add(shortenedUrl2);

       fullUrlRepository.save(fullUrl);
    }
}