package com.lumen.awsspringbootservice.service.impl;

import com.lumen.awsspringbootservice.dto.purchase.PurchaseDto;
import com.lumen.awsspringbootservice.dto.request.purchase.CreatePaymentSessionRequest;
import com.lumen.awsspringbootservice.dto.response.purchase.CreatePaymentSessionResponse;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.entity.Purchase;
import com.lumen.awsspringbootservice.entity.User;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.enums.PurchaseStatus;
import com.lumen.awsspringbootservice.exception.NotFoundException;
import com.lumen.awsspringbootservice.exception.PaymentException;
import com.lumen.awsspringbootservice.mapper.PurchaseMapper;
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
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final MoviePlanRepository moviePlanRepository;
    private final PurchaseMapper purchaseMapper;


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
        log.info("Creating purchase session for movie {} and movie plan {}, user {} ...", movieId, moviePlanId, userId);

        UUID userUUID = UUID.fromString(userId);
        UUID movieUUID = UUID.fromString(movieId);
        UUID planUUID = UUID.fromString(moviePlanId);

        User user = userRepository.findById(userUUID)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        Movie movie = movieRepository.findById(movieUUID)
                .orElseThrow(() -> new NotFoundException("Movie with id " + movieId + " not found"));
        MoviePlan requestedPlan = moviePlanRepository.findById(planUUID)
                .orElseThrow(() -> new NotFoundException("MoviePlan with id " + moviePlanId + " not found"));

        PlanType requestedType = requestedPlan.getType();

        List<Purchase> pendingPurchases = purchaseRepository
                .findAllByUserIdAndMovieId(userUUID, movieUUID).stream()
                .filter(p -> p.getPurchaseStatus() == PurchaseStatus.PENDING)
                .toList();

        purchaseRepository.deleteAll(pendingPurchases);

        List<Purchase> existingSuccessfulPurchases = purchaseRepository
                .findAllByUserIdAndMovieId(userUUID, movieUUID).stream()
                .filter(p -> p.getPurchaseStatus() == PurchaseStatus.SUCCESS)
                .toList();

        LocalDateTime now = LocalDateTime.now();

        for (Purchase existing : existingSuccessfulPurchases) {
            PlanType currentPlan = existing.getSelectedMoviePlan().getType();

            boolean isExpired = existing.getExpiresAt() != null && existing.getExpiresAt().isBefore(now);

            if ((currentPlan == PlanType.WEEK || currentPlan == PlanType.MONTH) && isExpired) {
                purchaseRepository.delete(existing);
                continue;
            }

            if (requestedType == PlanType.PREMIERE) {
                if (currentPlan == PlanType.PREMIERE) {
                    throw new IllegalStateException("Premiere already purchased");
                } else {
                    throw new IllegalStateException("Cannot purchase premiere — higher plan already active");
                }
            } else {

                if (currentPlan == PlanType.PREMIERE) {
                    continue;
                }

                throw new IllegalStateException("Movie is already purchased and still active");
            }
        }

        if (requestedType == PlanType.PREMIERE && movie.getPremiereDate().isBefore(now)) {
            throw new IllegalStateException("Cannot purchase premiere after release");
        }


        Purchase newPurchase = Purchase.builder()
                .user(user)
                .movie(movie)
                .selectedMoviePlan(requestedPlan)
                .purchaseStatus(PurchaseStatus.PENDING)
                .build();

        Purchase savedPurchase = purchaseRepository.save(newPurchase);

        CreatePaymentSessionRequest paymentSessionRequest = CreatePaymentSessionRequest.builder()
                .purchaseId(savedPurchase.getId().toString())
                .userId(userId)
                .webhookUrl(String.format("http://%s:%d/%s", serverAddress, serverPort, webhookEndpoint))
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

        log.info("Purchase session created successfully for user {}", userId);
        return response.getPaymentUrl();
    }


    public void setPurchaseSessionResult(String purchaseId, PurchaseStatus purchaseStatus) {
        UUID purchaseUUID = UUID.fromString(purchaseId);

        Purchase currentPurchase = purchaseRepository.findById(purchaseUUID)
                .orElseThrow(() -> new NotFoundException("Purchase with id " + purchaseId + " not found"));

        UUID userId = currentPurchase.getUser().getId();
        UUID movieId = currentPurchase.getMovie().getId();

        if (purchaseStatus == PurchaseStatus.FAILED) {
            if (currentPurchase.getPurchaseStatus() == PurchaseStatus.PENDING) {
                log.info("Removing failed PENDING purchase {}", purchaseId);
                purchaseRepository.delete(currentPurchase);
            }
            return;
        }

        if (purchaseStatus == PurchaseStatus.SUCCESS) {
            List<Purchase> existingSuccessfulPurchases = purchaseRepository
                    .findAllByUserIdAndMovieId(userId, movieId).stream()
                    .filter(p -> !p.getId().equals(purchaseUUID))
                    .filter(p -> p.getPurchaseStatus() == PurchaseStatus.SUCCESS)
                    .toList();

            if (!existingSuccessfulPurchases.isEmpty()) {
                Purchase oldPurchase = existingSuccessfulPurchases.get(0);

                log.info("Replacing existing SUCCESS purchase {} with new data from {}", oldPurchase.getId(), currentPurchase.getId());

                oldPurchase.setSelectedMoviePlan(currentPurchase.getSelectedMoviePlan());
                oldPurchase.setPurchasedAt(LocalDateTime.now());
                oldPurchase.setExpiresAt(oldPurchase.getPurchasedAt()
                        .plusDays(oldPurchase.getSelectedMoviePlan().getType().getDuration()));

                purchaseRepository.save(oldPurchase);

                purchaseRepository.delete(currentPurchase);
            } else {

                currentPurchase.setPurchaseStatus(PurchaseStatus.SUCCESS);
                currentPurchase.setPurchasedAt(LocalDateTime.now());
                currentPurchase.setExpiresAt(currentPurchase.getPurchasedAt()
                        .plusDays(currentPurchase.getSelectedMoviePlan().getType().getDuration()));

                purchaseRepository.save(currentPurchase);
            }
        }
    }


    public List<PurchaseDto> getActivePurchasedMovies(String userId) {
        UUID userUUID = UUID.fromString(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Purchase> activePurchases = purchaseRepository
                .findAllByUserId(userUUID).stream()
                .filter(p -> p.getPurchaseStatus() == PurchaseStatus.SUCCESS)
                .filter(p -> {
                    PlanType type = p.getSelectedMoviePlan().getType();
                    return type == PlanType.FULL || (p.getExpiresAt() != null && p.getExpiresAt().isAfter(now));
                })
                .toList();

        return activePurchases.stream()
                .map(purchaseMapper::toDto)
                .toList();
    }

}
