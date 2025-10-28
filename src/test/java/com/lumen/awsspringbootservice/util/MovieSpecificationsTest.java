package com.lumen.awsspringbootservice.util;

import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.entity.Review;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.repository.BaseRepositoryTest;
import com.lumen.awsspringbootservice.repository.MovieRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MovieSpecifications Integration Tests")
class MovieSpecificationsTest extends BaseRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Nested
    @DisplayName("Title Specification")
    class TitleSpecification {

        @Test
        @DisplayName("Should find movie by title containing substring (case-insensitive)")
        void shouldFindByTitle() {
            Movie movie = buildMovie();
            movie.setTitle("The Lord of the Rings");
            movieRepository.save(movie);

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.title("rings"))
            );

            assertEquals(1, result.size());
            assertTrue(result.get(0).getTitle().contains("Rings"));
        }

        @Test
        @DisplayName("Should return all movies when title is null")
        void shouldReturnAllWhenTitleNull() {
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.title(null))
            );

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Author Specification")
    class AuthorSpecification {

        @Test
        @DisplayName("Should find movie by author containing substring")
        void shouldFindByAuthor() {
            Movie movie = buildMovie();
            movie.setAuthor("Christopher Nolan");
            movieRepository.save(movie);

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.author("nolan"))
            );

            assertEquals(1, result.size());
            assertTrue(result.get(0).getAuthor().toLowerCase().contains("nolan"));
        }

        @Test
        @DisplayName("Should return all movies when author is null")
        void shouldReturnAllWhenAuthorNull() {
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.author(null))
            );

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Genre Specification")
    class GenreSpecification {

        @Test
        @DisplayName("Should find movie by multiple genres (AND logic)")
        void shouldFindByGenres() {
            Movie movie = buildMovie();
            movie.setGenres(new HashSet<>(Set.of(Genre.DRAMA, Genre.FANTASY, Genre.ACTION)));
            movieRepository.save(movie);

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.genres(List.of(Genre.DRAMA, Genre.FANTASY)))
            );

            assertEquals(1, result.size());
            assertTrue(result.get(0).getGenres().containsAll(List.of(Genre.DRAMA, Genre.FANTASY)));
        }

        @Test
        @DisplayName("Should return all movies when genre list is null or empty")
        void shouldReturnAllWhenGenresNull() {
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.genres(null))
            );

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Plan Type Specification")
    class PlanTypeSpecification {

        @Test
        @DisplayName("Should find movies that have all specified plan types")
        void shouldFindByPlanTypes() {
            Movie movie = buildMovie();

            MoviePlan plan1 = MoviePlan.builder()
                    .type(PlanType.MONTH)
                    .price(BigDecimal.valueOf(10))
                    .movie(movie)
                    .build();

            MoviePlan plan2 = MoviePlan.builder()
                    .type(PlanType.WEEK)
                    .price(BigDecimal.valueOf(4))
                    .movie(movie)
                    .build();
            movie.setMoviePlans(List.of(plan1, plan2));
            movieRepository.save(movie);

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.planTypes(List.of(PlanType.MONTH, PlanType.WEEK)))
            );

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return all movies when plan type filter is null or empty")
        void shouldReturnAllWhenPlanTypesNull() {
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.planTypes(null))
            );

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Rating Specification")
    class RatingSpecification {

        @Test
        @DisplayName("Should find movies with average star rating above threshold")
        void shouldFindByMinRating() {
            // given
            Movie highRated = buildMovie();

            Review review1 = new Review();
            review1.setStar(5);
            review1.setMovie(highRated);

            Review review2 = new Review();
            review2.setStar(4);
            review2.setMovie(highRated);

            highRated.setReviews(List.of(review1, review2));
            movieRepository.save(highRated);

            Movie lowRated = buildMovie();
            Review lowReview = new Review();
            lowReview.setStar(2);
            lowReview.setMovie(lowRated);
            lowRated.setReviews(List.of(lowReview));
            movieRepository.save(lowRated);

            // when
            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.minRating(4.0))
            );

            // then
            assertEquals(1, result.size());
            assertEquals(highRated.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Should return all movies when rating filter is null")
        void shouldReturnAllWhenRatingNull() {
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.minRating(null))
            );

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Premiere Date Specification")
    class PremiereDateSpecification {

        @Test
        @DisplayName("Should find movies after given date")
        void shouldFindAfterDate() {
            Movie oldMovie = buildMovie();
            oldMovie.setPremiereDate(LocalDate.of(2000, 1, 1).atStartOfDay());
            movieRepository.save(oldMovie);

            Movie newMovie = buildMovie();
            newMovie.setPremiereDate(LocalDate.of(2023, 1, 1).atStartOfDay());
            movieRepository.save(newMovie);

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.premiereDateAfter(LocalDate.of(2022, 1, 1)))
            );

            assertEquals(1, result.size());
            assertEquals(newMovie.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Should find movies before given date")
        void shouldFindBeforeDate() {
            Movie oldMovie = buildMovie();
            oldMovie.setPremiereDate(LocalDate.of(2000, 1, 1).atStartOfDay());
            movieRepository.save(oldMovie);

            Movie newMovie = buildMovie();
            newMovie.setPremiereDate(LocalDate.of(2023, 1, 1).atStartOfDay());
            movieRepository.save(newMovie);

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.premiereDateBefore(LocalDate.of(2010, 1, 1)))
            );

            assertEquals(1, result.size());
            assertEquals(oldMovie.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Should return all movies when date filter is null")
        void shouldReturnAllWhenDateIsNull() {
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.premiereDateBefore(null))
            );

            assertEquals(2, result.size());
        }
    }
}
