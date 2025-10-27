package com.lumen.awsspringbootservice.dto.purchase;

import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.User;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.enums.PurchaseStatus;

import java.time.LocalDateTime;

public class PurchaseDto {
    private String id;

    private User user;

    private Movie movie;

    private PlanType selectedPlanType;

    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;

    private PurchaseStatus purchaseStatus;
}
