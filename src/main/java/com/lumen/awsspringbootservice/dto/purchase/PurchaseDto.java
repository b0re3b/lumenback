package com.lumen.awsspringbootservice.dto.purchase;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.enums.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseDto {
    private String id;

    private String userId;

    private MovieDto movieDto;

    private PlanType selectedPlanType;

    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;

    private PurchaseStatus purchaseStatus;
}

