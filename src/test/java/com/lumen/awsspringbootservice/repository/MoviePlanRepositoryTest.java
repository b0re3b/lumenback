package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.enums.PlanType;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import com.lumen.awsspringbootservice.entity.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.profiles.active=test"
})
@DisplayName("MovieRepository Integration Tests")
class MoviePlanRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MoviePlanRepository moviePlanRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Nested
    @DisplayName("Find By Id Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return movie plan when id exists")
        void shouldReturnMoviePlanWhenIdExists() {
            // given
            Movie movie = buildMovie();
            movie = movieRepository.save(movie);

            MoviePlan plan = buildMoviePlan(movie, PlanType.MONTH);
            MoviePlan saved = moviePlanRepository.save(plan);

            // when
            Optional<MoviePlan> found = moviePlanRepository.findById(saved.getId());

            // then
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(movie.getId(), found.get().getMovie().getId());
            assertEquals(PlanType.MONTH, found.get().getType());
            assertEquals(new BigDecimal("9.99"), found.get().getPrice());
        }

        @Test
        @DisplayName("Should return empty when id does not exist")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            // when
            Optional<MoviePlan> found = moviePlanRepository.findById(UUID.randomUUID());

            // then
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Save MoviePlan Tests")
    class SaveMoviePlanTests {

        @Test
        @DisplayName("Should save and return movie plan")
        void shouldSaveAndReturnMoviePlan() {
            // given
            Movie movie = buildMovie();
            movie = movieRepository.save(movie);

            MoviePlan plan = buildMoviePlan(movie, PlanType.WEEK);

            // when
            MoviePlan saved = moviePlanRepository.save(plan);

            // then
            assertNotNull(saved.getId());
            assertEquals(movie, saved.getMovie());
            assertEquals(PlanType.WEEK, saved.getType());
            assertEquals(new BigDecimal("9.99"), saved.getPrice());
        }

        @Test
        @DisplayName("Should throw exception when saving null")
        void shouldThrowExceptionWhenSavingNull() {
            // then
            assertThrows(InvalidDataAccessApiUsageException.class,
                    () -> moviePlanRepository.save(null));
        }
    }
}