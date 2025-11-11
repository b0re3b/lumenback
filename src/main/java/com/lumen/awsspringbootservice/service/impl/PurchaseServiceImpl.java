package com.lumen.awsspringbootservice.service.impl;

import com.lumen.awsspringbootservice.dto.request.purchase.CreatePaymentSessionRequest;
import com.lumen.awsspringbootservice.dto.response.purchase.CreatePaymentSessionResponse;
import com.lumen.awsspringbootservice.entity.Purchase;
import com.lumen.awsspringbootservice.enums.PurchaseStatus;
import com.lumen.awsspringbootservice.exception.NotFoundException;
import com.lumen.awsspringbootservice.exception.PaymentException;
import com.lumen.awsspringbootservice.repository.MoviePlanRepository;
import com.lumen.awsspringbootservice.repository.MovieRepository;
import com.lumen.awsspringbootservice.repository.PurchaseRepository;
import com.lumen.awsspringbootservice.repository.UserRepository;
import com.lumen.awsspringbootservice.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final MoviePlanRepository moviePlanRepository;


    @Value("${app.payment.provider.url}")
    private String paymentUrl;

    @Value("${app.purchase.webhook.endpoint}")
    private String webhookEndpoint;

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.port:8080}")
    private int serverPort;

    private final WebClient webClient = WebClient.create();

    @Transactional
    public String createPurchaseSession(String movieId, String moviePlanId, String userId) {
        if (purchaseRepository.findByUserIdAndSelectedMoviePlanId(UUID.fromString(userId), UUID.fromString(moviePlanId)).isPresent()) {
            log.error("Purchase is already in process");
            throw new IllegalStateException("Purchase is already in process");
        }

        Purchase purchase = Purchase.builder()
                .user(userRepository
                        .findById(UUID.fromString(userId))
                        .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found")))
                .movie(movieRepository
                        .findById(UUID.fromString(movieId))
                        .orElseThrow(() -> new NotFoundException("Movie with id " + movieId + " not found")))
                .selectedMoviePlan(moviePlanRepository.findById(UUID.fromString(moviePlanId))
                        .orElseThrow(() -> new NotFoundException("MoviePlan with id " + moviePlanId + " not found")))
                .purchaseStatus(PurchaseStatus.PENDING)
                .build();

        purchase = purchaseRepository.save(purchase);


        CreatePaymentSessionRequest paymentSessionRequest = CreatePaymentSessionRequest.builder()
                .purchaseId(purchase.getId().toString())
                .userId(userId)
                .webhookUrl(String.format(
                        "http://%s:%d/%s",
                        serverAddress, serverPort, webhookEndpoint
                ))
                .build();

        CreatePaymentSessionResponse response = webClient.post()
                .uri(paymentUrl)
                .bodyValue(paymentSessionRequest)
                .retrieve()
                .bodyToMono(CreatePaymentSessionResponse.class)
                .block();

        if (response == null) {
            log.error("Create payment session failed");
            throw new PaymentException("Create payment session failed");
        }

        return response.getPaymentUrl();
    }

    public void setPurchaseSessionResult(String purchaseId, PurchaseStatus purchaseStatus) {
        Purchase purchase = purchaseRepository.findById(UUID.fromString(purchaseId)).orElseThrow(() -> new NotFoundException("Purchase with id " + purchaseId + " not found"));

        purchase.setPurchaseStatus(purchaseStatus);
        if (purchaseStatus == PurchaseStatus.SUCCESS) {
            purchase.setPurchasedAt(LocalDateTime.now());
        }
        purchaseRepository.save(purchase);
    }
}
