package com.klezovich.urlshortener.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenedUrl {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="full_url_id", nullable=false)
    private FullUrl fullUrl;

    private String shortenedUrl;
    @Column(unique = true)
    private String shortenedUrlKey;
    private String ownerName;

    @OneToMany(mappedBy="shortenedUrl", cascade = CascadeType.ALL)
    private List<ShortenedUrlAccessStats> accessStats = new ArrayList<>();
}
