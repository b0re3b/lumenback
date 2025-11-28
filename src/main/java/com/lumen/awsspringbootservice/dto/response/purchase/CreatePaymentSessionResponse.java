package com.lumen.awsspringbootservice.dto.response.purchase;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePaymentSessionResponse {
    private String paymentUrl;
}
