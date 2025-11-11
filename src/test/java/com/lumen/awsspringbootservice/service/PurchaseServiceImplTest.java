package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.response.purchase.CreatePaymentSessionResponse;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.entity.Purchase;
import com.lumen.awsspringbootservice.entity.User;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseServiceImpl Simplified Unit Tests")
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
    void setUp() {
        ReflectionTestUtils.setField(purchaseService, "paymentUrl",
                "http://mock-payment-service:8082/api/v1/payments/create-session");

        ReflectionTestUtils.setField(purchaseService, "webhookEndpoint",
                "api/v1/lumen/purchases/callback");

        ReflectionTestUtils.setField(purchaseService, "serverAddress", "localhost");
        ReflectionTestUtils.setField(purchaseService, "serverPort", 8080);
    }

    @Nested
    @DisplayName("createPurchaseSession Tests")
    class CreatePurchaseSessionTests {

        @Test
        @DisplayName("Should create purchase and return payment URL successfully")
        void shouldReturnPaymentUrlWhenSuccess() {
            UUID userId = UUID.randomUUID();
            UUID movieId = UUID.randomUUID();
            UUID moviePlanId = UUID.randomUUID();

            User user = new User();
            Movie movie = new Movie();
            MoviePlan plan = new MoviePlan();
            Purchase purchase = Purchase.builder().id(UUID.randomUUID()).build();

            when(purchaseRepository.findByUserIdAndSelectedMoviePlanId(userId, moviePlanId))
                    .thenReturn(Optional.empty());
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(moviePlanRepository.findById(moviePlanId)).thenReturn(Optional.of(plan));
            when(purchaseRepository.save(any())).thenReturn(purchase);

            WebClient mockWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
            CreatePaymentSessionResponse mockResponse = new CreatePaymentSessionResponse();
            mockResponse.setPaymentUrl("https://mock.payment/session123");

            when(mockWebClient.post()
                    .uri(anyString())
                    .bodyValue(any())
                    .retrieve()
                    .bodyToMono(CreatePaymentSessionResponse.class)
                    .block())
                    .thenReturn(mockResponse);

            // підміняємо приватне поле webClient у сервісі
            ReflectionTestUtils.setField(purchaseService, "webClient", mockWebClient);

            // when
            String result = purchaseService.createPurchaseSession(
                    movieId.toString(),
                    moviePlanId.toString(),
                    userId.toString());

            // then
            assertEquals("https://mock.payment/session123", result);
            verify(purchaseRepository).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalStateException if purchase already exists")
        void shouldThrowWhenPurchaseAlreadyExists() {
            UUID userId = UUID.randomUUID();
            UUID moviePlanId = UUID.randomUUID();

            when(purchaseRepository.findByUserIdAndSelectedMoviePlanId(userId, moviePlanId))
                    .thenReturn(Optional.of(new Purchase()));

            assertThrows(IllegalStateException.class, () ->
                    purchaseService.createPurchaseSession(
                            UUID.randomUUID().toString(),
                            moviePlanId.toString(),
                            userId.toString()));
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            UUID movieId = UUID.randomUUID();
            UUID moviePlanId = UUID.randomUUID();

            when(purchaseRepository.findByUserIdAndSelectedMoviePlanId(userId, moviePlanId))
                    .thenReturn(Optional.empty());
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    purchaseService.createPurchaseSession(
                            movieId.toString(),
                            moviePlanId.toString(),
                            userId.toString()));
        }

        @Test
        @DisplayName("Should throw PaymentException if response is null")
        void shouldThrowPaymentExceptionWhenResponseNull() {
            UUID userId = UUID.randomUUID();
            UUID movieId = UUID.randomUUID();
            UUID moviePlanId = UUID.randomUUID();

            User user = new User();
            Movie movie = new Movie();
            MoviePlan plan = new MoviePlan();
            Purchase purchase = Purchase.builder().id(UUID.randomUUID()).build();

            when(purchaseRepository.findByUserIdAndSelectedMoviePlanId(userId, moviePlanId))
                    .thenReturn(Optional.empty());
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(moviePlanRepository.findById(moviePlanId)).thenReturn(Optional.of(plan));
            when(purchaseRepository.save(any())).thenReturn(purchase);

            WebClient mockWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
            when(mockWebClient.post()
                    .uri(anyString())
                    .bodyValue(any())
                    .retrieve()
                    .bodyToMono(CreatePaymentSessionResponse.class)
                    .block())
                    .thenReturn(null);

            ReflectionTestUtils.setField(purchaseService, "webClient", mockWebClient);

            assertThrows(PaymentException.class, () ->
                    purchaseService.createPurchaseSession(
                            movieId.toString(),
                            moviePlanId.toString(),
                            userId.toString()));
        }
    }

    @Nested
    @DisplayName("setPurchaseSessionResult Tests")
    class SetPurchaseSessionResultTests {

        @Test
        @DisplayName("Should update status and set purchasedAt when SUCCESS")
        void shouldSetPurchasedAtWhenSuccess() {
            UUID purchaseId = UUID.randomUUID();
            Purchase purchase = new Purchase();

            when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

            purchaseService.setPurchaseSessionResult(purchaseId.toString(), PurchaseStatus.SUCCESS);

            assertEquals(PurchaseStatus.SUCCESS, purchase.getPurchaseStatus());
            assertNotNull(purchase.getPurchasedAt());
            verify(purchaseRepository).save(purchase);
        }

        @Test
        @DisplayName("Should update status without purchasedAt when FAILED")
        void shouldSetStatusWithoutPurchasedAtWhenFailed() {
            UUID purchaseId = UUID.randomUUID();
            Purchase purchase = new Purchase();

            when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

            purchaseService.setPurchaseSessionResult(purchaseId.toString(), PurchaseStatus.FAILED);

            assertEquals(PurchaseStatus.FAILED, purchase.getPurchaseStatus());
            assertNull(purchase.getPurchasedAt());
            verify(purchaseRepository).save(purchase);
        }

        @Test
        @DisplayName("Should throw NotFoundException when purchase not found")
        void shouldThrowWhenPurchaseNotFound() {
            UUID purchaseId = UUID.randomUUID();
            when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    purchaseService.setPurchaseSessionResult(purchaseId.toString(), PurchaseStatus.SUCCESS));
        }
    }
}
