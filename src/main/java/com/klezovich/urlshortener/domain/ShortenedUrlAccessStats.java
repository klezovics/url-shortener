package com.klezovich.urlshortener.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenedUrlAccessStats {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="shortened_url_id", nullable=false)
    private ShortenedUrl shortenedUrl;

    private String visitorName;
    private int visitCount;

    public void incrementVisitCount() {
        visitCount++;
    }
}
