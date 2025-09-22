package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.profiles.active=test"
})
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
            assertEquals(saved.getS3Key(), found.get().getS3Key());
            assertEquals(saved.getAuthor(), found.get().getAuthor());
            assertEquals(saved.getPhotoUrl(), found.get().getPhotoUrl());
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
            assertEquals(movie.getS3Key(), saved.getS3Key());
            assertEquals(movie.getAuthor(), saved.getAuthor());
            assertEquals(movie.getPhotoUrl(), saved.getPhotoUrl());
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
            assertEquals(saved.getS3Key(), updated.getS3Key());
            assertEquals(saved.getAuthor(), updated.getAuthor());
            assertEquals(saved.getPhotoUrl(), updated.getPhotoUrl());
        }
    }
}