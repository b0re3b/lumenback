package com.lumen.awsspringbootservice.controller.impl;

import com.lumen.awsspringbootservice.controller.PurchaseCallbackController;
import com.lumen.awsspringbootservice.dto.request.purchase.PaymentSessionResultRequest;
import com.lumen.awsspringbootservice.dto.response.MessageResponse;
import com.lumen.awsspringbootservice.enums.PurchaseStatus;
import com.lumen.awsspringbootservice.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/lumen/purchases/callback")
public class PurchaseCallbackControllerImpl implements PurchaseCallbackController {

    private final PurchaseService purchaseService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageResponse> processRequest(
            @RequestBody @Valid PaymentSessionResultRequest request
    ) {
        purchaseService.setPurchaseSessionResult(request.getPurchaseId(), request.getStatus());
        if (request.getStatus().equals(PurchaseStatus.SUCCESS)) {
            return ResponseEntity.status(HttpStatus.OK).body(MessageResponse.builder().message("Purchase has been approved").build());
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(MessageResponse.builder().message("Purchase has been rejected").build());
        }
    }
}
