package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.*;
import com.lumen.awsspringbootservice.entity.Record;
import com.lumen.awsspringbootservice.entity.User;
import com.lumen.awsspringbootservice.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@DisplayName("MovieRepository Integration Tests")
public abstract class BaseRepositoryTest {

    public User buildUser() {
        return User.builder()
                .email("user_" + UUID.randomUUID() + "@example.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .build();
    }

    protected Movie buildMovie() {
        return Movie.builder()
                .title("Movie " + UUID.randomUUID())
                .description("Some test description")
                .premiereDate(LocalDateTime.now().minusDays(10))
                .genres(Set.of(Genre.DRAMA, Genre.FANTASY))
                .s3Key("test-s3")
                .author("author")
                .photoUrl("https://photo.example.com")
                .build();
    }

    protected MoviePlan buildMoviePlan(Movie movie, PlanType type) {
        return MoviePlan.builder()
                .movie(movie)
                .type(type)
                .price(new BigDecimal("9.99"))
                .build();
    }

    protected Purchase buildPurchase(User user, Movie movie, PlanType planType) {
        return Purchase.builder()
                .user(user)
                .movie(movie)
                .selectedPlanType(planType)
                .purchasedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    protected Record buildRecord(User user, Movie movie, Purchase purchases) {
        return Record.builder()
                .user(user)
                .movie(movie)
                .purchase(purchases)
                .purchasedAt(LocalDateTime.now())
                .build();
    }

    protected Review buildReview(User user, Movie movie) {
        return Review.builder()
                .author(user)
                .movie(movie)
                .star(4)
                .comment("Good movie")
                .createdAt(LocalDateTime.now())
                .build();
    }

    protected Playlist buildPlaylist(User user) {
        return Playlist.builder()
                .author(user)
                .name("My Playlist")
                .createdAt(LocalDateTime.now())
                .build();
    }
}