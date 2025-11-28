package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.entity.Purchase;
import com.lumen.awsspringbootservice.entity.User;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.enums.Role;
import com.lumen.awsspringbootservice.repository.PurchaseRepository;
import com.lumen.awsspringbootservice.service.impl.PurchaseReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@DisplayName("PurchaseReportServiceImpl Unit Tests")
@ExtendWith(MockitoExtension.class)
class PurchaseReportServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private SimpleEmailService simpleEmailService;

    @InjectMocks
    private PurchaseReportServiceImpl purchaseReportService;

    private Movie movie;
    private User user;
    private MoviePlan plan;
    private Purchase purchase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(purchaseReportService, "reportFormats", List.of("pdf", "xlsx"));
        ReflectionTestUtils.setField(purchaseReportService, "reportPeriod", Period.ofWeeks(4));

        user = User.builder()
                .email("user@example.com")
                .password("pass")
                .role(Role.CUSTOMER)
                .build();

        movie = Movie.builder()
                .id(UUID.randomUUID())
                .title("Inception")
                .genres(Set.of(Genre.SCIENCE_FICTION, Genre.ACTION))
                .build();

        plan = MoviePlan.builder()
                .price(BigDecimal.valueOf(12.99))
                .type(PlanType.MONTH)
                .movie(movie)
                .build();

        purchase = Purchase.builder()
                .movie(movie)
                .user(user)
                .selectedMoviePlan(plan)
                .purchasedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Nested
    @DisplayName("When there are no purchases")
    class EmptyReportTests {

        @Test
        @DisplayName("Should generate and send empty report")
        void shouldGenerateAndSendEmptyReport() {
            // given
            when(purchaseRepository.findAllByPurchasedAtBetween(any(), any()))
                    .thenReturn(List.of());

            // when
            purchaseReportService.generateAndSendReport(null, null);

            // then
            verify(purchaseRepository, times(1)).findAllByPurchasedAtBetween(any(), any());
            verify(simpleEmailService, times(1)).sendReports(anyMap());
        }
    }

    @Nested
    @DisplayName("When purchases exist")
    class NonEmptyReportTests {

        @Test
        @DisplayName("Should aggregate, generate, and send report")
        void shouldGenerateReportWithData() {
            // given
            when(purchaseRepository.findAllByPurchasedAtBetween(any(), any()))
                    .thenReturn(List.of(purchase));

            // when
            purchaseReportService.generateAndSendReport(null, null);

            // then
            verify(purchaseRepository, times(1)).findAllByPurchasedAtBetween(any(), any());
            verify(simpleEmailService, times(1)).sendReports(argThat(map -> {
                if (map.isEmpty()) return false;

                return map.keySet().stream().allMatch(key ->
                        key.endsWith(".pdf") || key.endsWith(".xlsx")
                );
            }));
        }
    }

}

