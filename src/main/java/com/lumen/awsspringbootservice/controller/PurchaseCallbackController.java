package com.lumen.awsspringbootservice.controller;

import com.lumen.awsspringbootservice.dto.request.purchase.PaymentSessionResultRequest;
import com.lumen.awsspringbootservice.dto.response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("api/v1/lumen/purchases/callback")
public interface PurchaseCallbackController {

    @PostMapping
    ResponseEntity<MessageResponse> processRequest(@RequestBody @Valid PaymentSessionResultRequest request);
}
