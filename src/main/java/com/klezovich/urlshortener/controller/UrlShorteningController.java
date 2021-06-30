package com.klezovich.urlshortener.controller;

import com.klezovich.urlshortener.controller.dto.ShortenUrlRequest;
import com.klezovich.urlshortener.controller.dto.ShortenUrlResponse;
import com.klezovich.urlshortener.domain.ShortenedUrl;
import com.klezovich.urlshortener.service.UrlShorteningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.security.Principal;

@Controller
@RequestMapping("/")
@Slf4j
public class UrlShorteningController {

    @Autowired
    private final UrlShorteningService service;

    public UrlShorteningController(UrlShorteningService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String mainPage(Model m) {
        m.addAttribute("shortenUrlRequest", new ShortenUrlRequest());
        return "index";
    }

    @GetMapping("/e/{shortenedUrlKey}")
    public ResponseEntity<Void> redirectToPageByShortenedUrl(
            @PathVariable("shortenedUrlKey") String key,
            Principal principal) {

        var username = getUsername(principal);

        var fullUrl = service.getFullUrlByShortenedUrlKey(key,username);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(fullUrl.getFullUrl())).build();
    }

    @PostMapping("/shorten")
    public RedirectView shortenUrl(
            @ModelAttribute ShortenUrlRequest request,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        var url = request.getUrl();
        var username = getUsername(principal);

        var shortenedUrl = service.shortenUrlForUser(url, username);

        redirectAttributes.addFlashAttribute("shortenedUrlResponse", toShortenUrlResponse(shortenedUrl));
        var redirectView = new RedirectView("/", true);
        return redirectView;
    }

    private String getUsername(Principal principal) {
        if(principal == null) {
            return "anonymous";
        }

        return principal.getName();
    }

    ShortenUrlResponse toShortenUrlResponse(ShortenedUrl shortenedUrl) {
        var timesShortened = shortenedUrl.getFullUrl().getTimesShortened();

        int timesAccessed = 0;
        for(var stat : shortenedUrl.getAccessStats()) {
            timesAccessed+=stat.getVisitCount();
        }

        return ShortenUrlResponse.builder()
                .shortenedUrl(shortenedUrl.getShortenedUrl())
                .timesShortened(timesShortened)
                .timesAccessed(timesAccessed)
                .build();
    }
}
