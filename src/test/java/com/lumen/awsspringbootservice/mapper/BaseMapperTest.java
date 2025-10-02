package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@DisplayName("Base Mapper Test")
public abstract class BaseMapperTest {
    protected Movie buildMovieEntity() {
        return Movie.builder()
                .id(UUID.randomUUID())
                .author("author_" + UUID.randomUUID())
                .title("Movie " + UUID.randomUUID())
                .description("Some test description")
                .premiereDate(LocalDateTime.now().minusDays(10))
                .genres(Set.of(Genre.DRAMA, Genre.FANTASY))
                .s3Key("test-s3-key")
                .photoUrl("https://photo.example.com/movie.jpg")
                .moviePlans(List.of(buildMoviePlanEntity(null, PlanType.WEEK)))
                .build();
    }

    protected MoviePlan buildMoviePlanEntity(Movie movie, PlanType type) {
        return MoviePlan.builder()
                .id(UUID.randomUUID())
                .movie(movie)
                .type(type)
                .price(new BigDecimal("9.99"))
                .build();
    }

    protected MovieDto buildMovieDto() {
        return MovieDto.builder()
                .id(UUID.randomUUID().toString())
                .author("author_" + UUID.randomUUID())
                .title("Movie DTO " + UUID.randomUUID())
                .description("DTO test description")
                .premiereDate(LocalDateTime.now().minusDays(5))
                .genres(Set.of(Genre.FANTASY))
                .s3Key("dto-s3-key")
                .photoUrl("https://photo.example.com/dto.jpg")
                .moviePlanShortDtoList(List.of(buildMoviePlanShortDto(PlanType.MONTH)))
                .build();
    }

    protected MoviePlanShortDto buildMoviePlanShortDto(PlanType type) {
        return MoviePlanShortDto.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .price(new BigDecimal("19.99"))
                .build();
    }
}