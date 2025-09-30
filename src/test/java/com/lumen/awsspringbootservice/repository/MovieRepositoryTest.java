package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MovieRepository Integration Tests")
class MovieRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Nested
    @DisplayName("Find By Id Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return movie when id exists")
        void shouldReturnMovieWhenIdExists() {
            // given
            Movie movie = buildMovie();
            Movie saved = movieRepository.save(movie);

            // when
            Optional<Movie> found = movieRepository.findById(saved.getId());

            // then
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(saved.getTitle(), found.get().getTitle());
            assertEquals(saved.getDescription(), found.get().getDescription());
            assertEquals(saved.getPremiereDate(), found.get().getPremiereDate());
            assertEquals(saved.getGenres(), found.get().getGenres());
            assertEquals(saved.getVideoS3Key(), found.get().getVideoS3Key());
            assertEquals(saved.getAuthor(), found.get().getAuthor());
            assertEquals(saved.getPosterS3Key(), found.get().getPosterS3Key());
        }

        @Test
        @DisplayName("Should return empty when id does not exist")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            // when
            Optional<Movie> found = movieRepository.findById(UUID.randomUUID());

            // then
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Save Movie Tests")
    class SaveMovieTests {

        @Test
        @DisplayName("Should save and return movie")
        void shouldSaveAndReturnMovie() {
            // given
            Movie movie = buildMovie();

            // when
            Movie saved = movieRepository.save(movie);

            // then
            assertNotNull(saved.getId());
            assertEquals(movie.getTitle(), saved.getTitle());
            assertEquals(movie.getDescription(), saved.getDescription());
            assertEquals(movie.getPremiereDate(), saved.getPremiereDate());
            assertEquals(movie.getGenres(), saved.getGenres());
            assertEquals(movie.getVideoS3Key(), saved.getVideoS3Key());
            assertEquals(movie.getAuthor(), saved.getAuthor());
            assertEquals(movie.getPosterS3Key(), saved.getPosterS3Key());
        }

        @Test
        @DisplayName("Should throw exception when saving null")
        void shouldThrowExceptionWhenSavingNull() {
            // then
            assertThrows(InvalidDataAccessApiUsageException.class,
                    () -> movieRepository.save(null));
        }
    }

    @Nested
    @DisplayName("Update Movie Tests")
    class UpdateMovieTests {

        @Test
        @DisplayName("Should update movie title")
        void shouldUpdateMovieTitle() {
            // given
            Movie movie = buildMovie();
            movie.setTitle("Old Title");
            Movie saved = movieRepository.save(movie);

            // when
            saved.setTitle("New Title");
            Movie updated = movieRepository.save(saved);

            // then
            assertEquals(saved.getId(), updated.getId());
            assertEquals("New Title", updated.getTitle());
            assertEquals(saved.getDescription(), updated.getDescription());
            assertEquals(saved.getPremiereDate(), updated.getPremiereDate());
            assertEquals(saved.getGenres(), updated.getGenres());
            assertEquals(saved.getVideoS3Key(), updated.getVideoS3Key());
            assertEquals(saved.getAuthor(), updated.getAuthor());
            assertEquals(saved.getPosterS3Key(), updated.getPosterS3Key());
        }
    }
}