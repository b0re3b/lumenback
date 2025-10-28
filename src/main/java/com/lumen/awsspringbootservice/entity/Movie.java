package com.lumen.awsspringbootservice.entity;

import com.lumen.awsspringbootservice.enums.Genre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "author")
    private String author;
    @Column(name = "title")
    private String title;
    @Column(name = "description")
    private String description;

    @ElementCollection(targetClass = Genre.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "movie", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoviePlan> moviePlans = new ArrayList<>();

    @Column(name = "premiere_date")
    private LocalDateTime premiereDate;

    @Column(name = "video_manifest_s3_key")
    private String videoManifestS3Key;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_fragments", joinColumns = @JoinColumn(name = "movie_id"))
    @OrderColumn(name = "fragment_index")
    private List<VideoFragment> videoFragments = new ArrayList<>();

    @Column(name = "poster_s3_key")
    private String posterS3Key;

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class VideoFragment {
        @Column(name = "video_fragment_s3_key")
        private String videoFragmentS3Key;

        @Column(name = "duration")
        private String duration;
    }

}
