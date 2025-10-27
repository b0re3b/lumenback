package com.lumen.awsspringbootservice.service.impl;

import com.lumen.awsspringbootservice.dto.request.purchase.CreatePaymentSessionRequest;
import com.lumen.awsspringbootservice.dto.request.purchase.PaymentSessionResultRequest;
import com.lumen.awsspringbootservice.entity.PaymentSession;
import com.lumen.awsspringbootservice.enums.PurchaseStatus;
import com.lumen.awsspringbootservice.exception.NotFoundException;
import com.lumen.awsspringbootservice.exception.PaymentException;
import com.lumen.awsspringbootservice.repository.PaymentSessionRepository;
import com.lumen.awsspringbootservice.service.MockPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockPaymentServiceImpl implements MockPaymentService {

    private final PaymentSessionRepository paymentSessionRepository;
    private final WebClient webClient = WebClient.create();

    @Override
    public String createPaymentSession(CreatePaymentSessionRequest request) {

        PaymentSession session = paymentSessionRepository.save(PaymentSession.builder()
                .status(PurchaseStatus.PENDING)
                .userId(request.getUserId())
                .purchaseId(request.getPurchaseId())
                .webhookUrl(request.getWebhookUrl()).build());

        return session.getId().toString();
    }

    @Override
    public PaymentSession getPaymentSessionById(String id) {
        PaymentSession session = paymentSessionRepository.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("PaymentSession with id " + id + " not found"));
        if (session.getStatus().equals(PurchaseStatus.PENDING)) {
            return session;
        }
        throw new NotFoundException("Pending PaymentSession with id " + id + " not found");
    }


    @Override
    @Transactional
    public void approvePaymentSession(String id) {
        PaymentSession session = paymentSessionRepository.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("PaymentSession with id " + id + " not found"));
        if (session.getStatus().equals(PurchaseStatus.PENDING)) {
            session.setStatus(PurchaseStatus.SUCCESS);
            paymentSessionRepository.save(session);

            PaymentSessionResultRequest approvePaymentSessionRequest = PaymentSessionResultRequest.builder()
                    .userId(session.getUserId())
                    .purchaseId(session.getPurchaseId())
                    .status(PurchaseStatus.SUCCESS)
                    .build();

            webClient.post()
                    .uri(session.getWebhookUrl())
                    .bodyValue(approvePaymentSessionRequest)
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            response -> {
                                log.error("Webhook callback failed for session {}", id);
                                throw new PaymentException("PaymentSession with id " + id + " webhook callback failed");
                            }
                    )
                    .toBodilessEntity()
                    .doOnSuccess(r -> log.info("Webhook callback success for session {}", id))
                    .block();


        } else {
            throw new IllegalStateException("Session not found or already processed: " + id);
        }
    }

    @Override
    @Transactional
    public void cancelPaymentSession(String id) {
        PaymentSession session = paymentSessionRepository.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("PaymentSession with id " + id + " not found"));
        if (session.getStatus().equals(PurchaseStatus.PENDING)) {
            session.setStatus(PurchaseStatus.FAILED);
            paymentSessionRepository.save(session);

            PaymentSessionResultRequest approvePaymentSessionRequest = PaymentSessionResultRequest.builder()
                    .userId(session.getUserId())
                    .purchaseId(session.getPurchaseId())
                    .status(PurchaseStatus.FAILED)
                    .build();

            webClient.post()
                    .uri(session.getWebhookUrl())
                    .bodyValue(approvePaymentSessionRequest)
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            response -> {
                                log.error("Webhook callback failed for session {}", id);
                                throw new PaymentException("PaymentSession with id " + id + " webhook callback failed");
                            }
                    )
                    .toBodilessEntity()
                    .doOnSuccess(r -> log.info("Webhook callback success for session {}", id))
                    .block();

        }
        throw new NotFoundException("Pending PaymentSession with id " + id + " not found");

    }
}
