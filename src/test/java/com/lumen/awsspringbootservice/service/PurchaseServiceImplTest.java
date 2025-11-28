package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.response.purchase.CreatePaymentSessionResponse;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.entity.Purchase;
import com.lumen.awsspringbootservice.entity.User;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.enums.PurchaseStatus;
import com.lumen.awsspringbootservice.exception.NotFoundException;
import com.lumen.awsspringbootservice.exception.PaymentException;
import com.lumen.awsspringbootservice.repository.MoviePlanRepository;
import com.lumen.awsspringbootservice.repository.MovieRepository;
import com.lumen.awsspringbootservice.repository.PurchaseRepository;
import com.lumen.awsspringbootservice.repository.UserRepository;
import com.lumen.awsspringbootservice.service.impl.PurchaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseServiceImpl Unit Tests (Updated Logic)")
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MoviePlanRepository moviePlanRepository;
    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(purchaseService, "paymentUrl", "http://mock/payment");
        ReflectionTestUtils.setField(purchaseService, "webhookEndpoint", "api/callback");
        ReflectionTestUtils.setField(purchaseService, "serverAddress", "localhost");
        ReflectionTestUtils.setField(purchaseService, "serverPort", 8080);
    }

    @Nested
    @DisplayName("createPurchaseSession")
    class CreatePurchaseSessionTests {

        @Test
        void shouldCreatePurchaseSession() {
            UUID movieId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID planId = UUID.randomUUID();

            User user = User.builder().id(userId).build();
            Movie movie = Movie.builder().id(movieId).premiereDate(LocalDateTime.now().plusDays(3)).build();
            MoviePlan plan = MoviePlan.builder().id(planId).type(PlanType.MONTH).price(BigDecimal.TEN).movie(movie).build();

            when(purchaseRepository.findAllByUserIdAndMovieId(userId, movieId))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(moviePlanRepository.findById(planId)).thenReturn(Optional.of(plan));

            Purchase saved = Purchase.builder().id(UUID.randomUUID()).user(user).movie(movie).selectedMoviePlan(plan).build();
            when(purchaseRepository.save(any())).thenReturn(saved);

            WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
            CreatePaymentSessionResponse response = new CreatePaymentSessionResponse("http://mock.payment/success");
            when(webClient.post().uri(anyString()).bodyValue(any()).retrieve().bodyToMono(eq(CreatePaymentSessionResponse.class)).block())
                    .thenReturn(response);

            ReflectionTestUtils.setField(purchaseService, "webClient", webClient);

            String result = purchaseService.createPurchaseSession(
                    movieId.toString(),
                    planId.toString(),
                    userId.toString()
            );

            assertEquals("http://mock.payment/success", result);
            verify(purchaseRepository).save(any());
        }

        @Test
        void shouldThrowIfMovieNotFound() {
            UUID movieId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID planId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    purchaseService.createPurchaseSession(
                            movieId.toString(),
                            planId.toString(),
                            userId.toString()
                    ));
        }

        @Test
        void shouldThrowIfPaymentResponseIsNull() {
            UUID movieId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID planId = UUID.randomUUID();

            User user = new User();
            Movie movie = Movie.builder().id(movieId).premiereDate(LocalDateTime.now().plusDays(2)).build();
            MoviePlan plan = MoviePlan.builder().id(planId).type(PlanType.PREMIERE).movie(movie).build();

            when(purchaseRepository.findAllByUserIdAndMovieId(userId, movieId))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(moviePlanRepository.findById(planId)).thenReturn(Optional.of(plan));
            when(purchaseRepository.save(any())).thenAnswer(inv -> {
                Purchase p = inv.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });

            WebClient mockWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
            when(mockWebClient.post().uri(anyString()).bodyValue(any()).retrieve()
                    .bodyToMono(eq(CreatePaymentSessionResponse.class)).block())
                    .thenReturn(null);

            ReflectionTestUtils.setField(purchaseService, "webClient", mockWebClient);

            assertThrows(PaymentException.class, () ->
                    purchaseService.createPurchaseSession(
                            movieId.toString(),
                            planId.toString(),
                            userId.toString()
                    ));
        }
    }

    @Nested
    @DisplayName("setPurchaseSessionResult")
    class SetPurchaseSessionResultTests {

        @Test
        void shouldRemovePendingWhenFailed() {
            UUID id = UUID.randomUUID();
            User user = User.builder().id(UUID.randomUUID()).build();
            Movie movie = Movie.builder().id(UUID.randomUUID()).build();

            Purchase purchase = Purchase.builder()
                    .id(id)
                    .user(user)
                    .movie(movie)
                    .purchaseStatus(PurchaseStatus.PENDING)
                    .build();

            when(purchaseRepository.findById(id)).thenReturn(Optional.of(purchase));

            purchaseService.setPurchaseSessionResult(id.toString(), PurchaseStatus.FAILED);

            verify(purchaseRepository).delete(purchase);
        }


        @Test
        void shouldReplaceOldSuccessWhenNewIsSuccess() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID movieId = UUID.randomUUID();

            MoviePlan newPlan = MoviePlan.builder().type(PlanType.MONTH).build();
            Movie movie = Movie.builder().id(movieId).build();
            User user = User.builder().id(userId).build();

            Purchase current = Purchase.builder().id(id).user(user).movie(movie).selectedMoviePlan(newPlan).build();

            Purchase existing = Purchase.builder().id(UUID.randomUUID())
                    .user(user).movie(movie)
                    .selectedMoviePlan(MoviePlan.builder().type(PlanType.PREMIERE).build())
                    .purchaseStatus(PurchaseStatus.SUCCESS)
                    .build();

            when(purchaseRepository.findById(id)).thenReturn(Optional.of(current));
            when(purchaseRepository.findAllByUserIdAndMovieId(userId, movieId)).thenReturn(List.of(existing));

            purchaseService.setPurchaseSessionResult(id.toString(), PurchaseStatus.SUCCESS);

            verify(purchaseRepository).save(existing);
            verify(purchaseRepository).delete(current);
        }

        @Test
        void shouldSetSuccessIfNoOtherExists() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID movieId = UUID.randomUUID();

            User user = User.builder().id(userId).build();
            Movie movie = Movie.builder().id(movieId).build();
            MoviePlan plan = MoviePlan.builder().type(PlanType.WEEK).build();

            Purchase purchase = Purchase.builder().id(id).user(user).movie(movie).selectedMoviePlan(plan).build();

            when(purchaseRepository.findById(id)).thenReturn(Optional.of(purchase));
            when(purchaseRepository.findAllByUserIdAndMovieId(userId, movieId)).thenReturn(Collections.emptyList());

            purchaseService.setPurchaseSessionResult(id.toString(), PurchaseStatus.SUCCESS);

            assertEquals(PurchaseStatus.SUCCESS, purchase.getPurchaseStatus());
            assertNotNull(purchase.getPurchasedAt());
            assertNotNull(purchase.getExpiresAt());

            verify(purchaseRepository).save(purchase);
        }

        @Test
        void shouldThrowWhenPurchaseNotFound() {
            UUID id = UUID.randomUUID();
            when(purchaseRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    purchaseService.setPurchaseSessionResult(id.toString(), PurchaseStatus.SUCCESS));
        }
    }
}
