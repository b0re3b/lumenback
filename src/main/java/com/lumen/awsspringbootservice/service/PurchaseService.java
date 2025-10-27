package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.enums.PurchaseStatus;

public interface PurchaseService {

    String createPurchaseSession(String movieId, String moviePlanId, String userId);

    void setPurchaseSessionResult(String purchaseId, PurchaseStatus purchaseStatus);
}
