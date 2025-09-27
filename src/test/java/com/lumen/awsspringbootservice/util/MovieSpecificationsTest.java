package com.lumen.awsspringbootservice.util;

import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.repository.BaseRepositoryTest;
import com.lumen.awsspringbootservice.repository.MovieRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

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
        @DisplayName("Should find movie by title containing substring")
        void shouldFindByTitle() {
            // given
            Movie movie = buildMovie();
            movie.setTitle("The Lord of the Rings");
            movieRepository.save(movie);

            // when
            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.title("rings"))
            );

            // then
            assertEquals(1, result.size());
            assertTrue(result.get(0).getTitle().contains("Rings"));
        }

        @Test
        @DisplayName("Should return all movies when title is null")
        void shouldReturnAllWhenTitleNull() {
            // given
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            // when
            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.title(null))
            );

            // then
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Genre Specification")
    class GenreSpecification {

        @Test
        @DisplayName("Should find movie by genre")
        void shouldFindByGenre() {
            // given
            Movie movie = buildMovie();
            movie.setGenres(new HashSet<>(Set.of(Genre.DRAMA, Genre.FANTASY)));
            movieRepository.save(movie);

            // when
            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.genre(Genre.DRAMA))
            );

            // then
            assertEquals(1, result.size());
            assertTrue(result.get(0).getGenres().contains(Genre.DRAMA));
        }

        @Test
        @DisplayName("Should return all movies when genre is null")
        void shouldReturnAllWhenGenreNull() {
            // given
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            // when
            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.genre(null))
            );

            // then
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Premiere Date Specification")
    class PremiereDateSpecification {

        @Test
        @DisplayName("Should find movies after given date")
        void shouldFindAfterDate() {
            // given
            Movie oldMovie = buildMovie();
            oldMovie.setPremiereDate(LocalDate.of(2000, 1, 1).atStartOfDay());
            movieRepository.save(oldMovie);

            Movie newMovie = buildMovie();
            newMovie.setPremiereDate(LocalDate.of(2023, 1, 1).atStartOfDay());
            movieRepository.save(newMovie);

            // when
            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.premiereDateAfter(LocalDate.of(2022, 1, 1)))
            );

            // then
            assertEquals(1, result.size());
            assertEquals(newMovie.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Should find movies before given date")
        void shouldFindBeforeDate() {
            // given
            Movie oldMovie = buildMovie();
            oldMovie.setPremiereDate(LocalDate.of(2000, 1, 1).atStartOfDay());
            movieRepository.save(oldMovie);

            Movie newMovie = buildMovie();
            newMovie.setPremiereDate(LocalDate.of(2023, 1, 1).atStartOfDay());
            movieRepository.save(newMovie);

            // when
            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.premiereDateBefore(LocalDate.of(2010, 1, 1)))
            );

            // then
            assertEquals(1, result.size());
            assertEquals(oldMovie.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Should return all movies when date filter is null")
        void shouldReturnAllWhenDateIsNull() {
            // given
            movieRepository.save(buildMovie());
            movieRepository.save(buildMovie());

            // when
            List<Movie> result = movieRepository.findAll(
                    Specification.allOf(MovieSpecifications.premiereDateBefore(null))
            );

            // then
            assertEquals(2, result.size());
        }
    }
}
