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

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "movie", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<MoviePlan> moviePlans = new ArrayList<>();

    @Column(name = "premiere_date")
    private LocalDateTime premiereDate;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "photo_url")
    private String photoUrl;
}
