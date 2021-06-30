package com.klezovich.urlshortener.controller;

import com.klezovich.urlshortener.repository.FullUrlRepository;
import com.klezovich.urlshortener.repository.ShortenedUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private final FullUrlRepository fullUrlRepository;

    @Autowired
    private final ShortenedUrlRepository shortenedUrlRepository;

    public AdminController(FullUrlRepository fullUrlRepository, ShortenedUrlRepository shortenedUrlRepository) {
        this.fullUrlRepository = fullUrlRepository;
        this.shortenedUrlRepository = shortenedUrlRepository;
    }

    @GetMapping("/statistics")
    public String showListOfFullUrls(Model m) {
        var shortenedUrls = fullUrlRepository.findAll();
        m.addAttribute("fullUrls", shortenedUrls);
        return "full-url-info";
    }

    @GetMapping("/shortened-urls/{full-url-id}")
    public String showShortnedUrlsForFullUrl(@PathVariable("full-url-id") Long fullUrlId, Model m) {
        var fullUrl = fullUrlRepository.findById(fullUrlId).get();
        m.addAttribute("shortenedUrls", fullUrl.getShortenedUrls());
        m.addAttribute("fullUrl", fullUrl.getFullUrl());
        return "shortened-url-info";
    }

    @GetMapping("/shortened-url-access-stats/{shortened-url-id}")
    public String showAccessStatisticsForShortenedUrl(@PathVariable("shortened-url-id") Long shortenedUrlId, Model m) {
        var shortenedUrl = shortenedUrlRepository.findById(shortenedUrlId).get();
        m.addAttribute("accessStats", shortenedUrl.getAccessStats());
        m.addAttribute("shortenedUrl", shortenedUrl.getShortenedUrl());
        return "shortened-url-access-stats";
    }
}
