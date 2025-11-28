package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.request.purchase.CreatePaymentSessionRequest;
import com.lumen.awsspringbootservice.entity.PaymentSession;

public interface MockPaymentService {

    String createPaymentSession(CreatePaymentSessionRequest request);

    void approvePaymentSession(String id);

    void cancelPaymentSession(String id);

    PaymentSession getPaymentSessionById(String id);

}
