package com.klezovich.urlshortener.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortenUrlResponse {

    private String shortenedUrl;
    private int timesShortened;
    private int timesAccessed;
}
