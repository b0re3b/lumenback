package com.lumen.awsspringbootservice.dto.request.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePurchaseRequest {
    private String moviePlanId;
}
