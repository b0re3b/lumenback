package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.entity.Purchase;
import com.lumen.awsspringbootservice.entity.User;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PurchaseRepository Integration Tests")
class PurchaseRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoviePlanRepository moviePlanRepository;

    @Nested
    @DisplayName("Find By User And Plan Tests")
    class FindByUserAndPlanTests {

        @Test
        @DisplayName("Should return purchase when user and plan match")
        void shouldReturnPurchaseWhenUserAndPlanMatch() {
            // given
            User user = userRepository.save(buildUser());
            Movie movie = movieRepository.save(buildMovie());
            MoviePlan plan = moviePlanRepository.save(buildMoviePlan(movie, PlanType.WEEK));
            Purchase purchase = purchaseRepository.save(buildPurchase(user, movie, plan));

            // when
            Optional<Purchase> found = purchaseRepository.findByUserIdAndSelectedMoviePlanId(
                    user.getId(), plan.getId()
            );

            // then
            assertTrue(found.isPresent());
            assertEquals(purchase.getId(), found.get().getId());
            assertEquals(user.getId(), found.get().getUser().getId());
            assertEquals(plan.getId(), found.get().getSelectedMoviePlan().getId());
        }

        @Test
        @DisplayName("Should return empty when user or plan do not match")
        void shouldReturnEmptyWhenUserOrPlanDoNotMatch() {
            // when
            Optional<Purchase> found = purchaseRepository.findByUserIdAndSelectedMoviePlanId(
                    UUID.randomUUID(), UUID.randomUUID()
            );

            // then
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Find All By Date Range Tests")
    class FindAllByDateRangeTests {

        @Test
        @DisplayName("Should return purchases within date range")
        void shouldReturnPurchasesWithinDateRange() {
            // given
            User user = userRepository.save(buildUser());
            Movie movie = movieRepository.save(buildMovie());
            MoviePlan plan = moviePlanRepository.save(buildMoviePlan(movie, PlanType.WEEK));

            LocalDateTime now = LocalDateTime.now();
            Purchase purchase = buildPurchase(user, movie, plan);
            purchase.setPurchasedAt(now.minusDays(3));
            purchaseRepository.save(purchase);

            // when
            List<Purchase> result = purchaseRepository.findAllByPurchasedAtBetween(
                    now.minusDays(5), now.minusDays(1)
            );

            // then
            assertEquals(1, result.size());
            assertEquals(purchase.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Should return empty when no purchases in range")
        void shouldReturnEmptyWhenNoPurchasesInRange() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // when
            List<Purchase> result = purchaseRepository.findAllByPurchasedAtBetween(
                    now.minusDays(10), now.minusDays(6)
            );

            // then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find Top Sales Tests")
    class FindTopSalesTests {

        @Test
        @DisplayName("Should return movies ordered by sales count")
        void shouldReturnMoviesOrderedBySalesCount() {
            // given
            User user = userRepository.save(buildUser());
            Movie movie1 = movieRepository.save(buildMovie());
            Movie movie2 = movieRepository.save(buildMovie());
            MoviePlan plan1 = moviePlanRepository.save(buildMoviePlan(movie1, PlanType.WEEK));
            MoviePlan plan2 = moviePlanRepository.save(buildMoviePlan(movie2, PlanType.WEEK));

            for (int i = 0; i < 3; i++) {
                purchaseRepository.save(buildPurchase(user, movie1, plan1));
            }
            purchaseRepository.save(buildPurchase(user, movie2, plan2));

            // when
            Page<UUID> result = purchaseRepository.findTopSales(PageRequest.of(0, 10));

            // then
            assertEquals(2, result.getContent().size());
            assertEquals(movie1.getId().toString(), result.getContent().get(0).toString());
        }
    }

    @Nested
    @DisplayName("Find Top Genres Tests")
    class FindTopGenresTests {

        @Test
        @DisplayName("Should return genres ordered by purchase count")
        void shouldReturnGenresOrderedByPurchaseCount() {
            // given
            User user = userRepository.save(buildUser());

            Movie movie1 = movieRepository.save(Movie.builder()
                    .title("Movie 1")
                    .description("desc")
                    .premiereDate(LocalDateTime.now().minusDays(5))
                    .genres(Set.of(Genre.ACTION, Genre.DRAMA))
                    .posterS3Key("poster1")
                    .author("author1")
                    .videoFragments(List.of(new Movie.VideoFragment("key1", "10.0")))
                    .build());

            Movie movie2 = movieRepository.save(Movie.builder()
                    .title("Movie 2")
                    .description("desc")
                    .premiereDate(LocalDateTime.now().minusDays(5))
                    .genres(Set.of(Genre.ACTION))
                    .posterS3Key("poster2")
                    .author("author2")
                    .videoFragments(List.of(new Movie.VideoFragment("key2", "10.0")))
                    .build());

            MoviePlan plan1 = moviePlanRepository.save(buildMoviePlan(movie1, PlanType.WEEK));
            MoviePlan plan2 = moviePlanRepository.save(buildMoviePlan(movie2, PlanType.WEEK));

            purchaseRepository.save(buildPurchase(user, movie1, plan1));
            purchaseRepository.save(buildPurchase(user, movie2, plan2));

            // when
            Page<Genre> result = purchaseRepository.findTopGenresOnly(PageRequest.of(0, 5));

            // then
            assertFalse(result.isEmpty());
            assertEquals(Genre.ACTION, result.getContent().get(0));
        }
    }
}
