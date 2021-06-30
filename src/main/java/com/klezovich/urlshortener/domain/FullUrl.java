package com.klezovich.urlshortener.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FullUrl {

    @Id
    @GeneratedValue
    private Long id;
    private String fullUrl;

    @OneToMany(mappedBy="fullUrl", cascade = CascadeType.ALL)
    private List<ShortenedUrl> shortenedUrls = new ArrayList<>();

    private int timesShortened;

    public void incrementTimesShortened() {
        timesShortened++;
    }
}
