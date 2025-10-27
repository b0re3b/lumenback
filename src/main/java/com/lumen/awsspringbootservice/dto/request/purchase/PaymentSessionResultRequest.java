package com.lumen.awsspringbootservice.dto.request.purchase;

import com.lumen.awsspringbootservice.enums.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentSessionResultRequest {
    private String userId;
    private String purchaseId;
    private PurchaseStatus status;
}
