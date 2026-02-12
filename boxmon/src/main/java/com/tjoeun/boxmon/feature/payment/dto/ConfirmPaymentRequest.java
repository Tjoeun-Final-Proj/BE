package com.tjoeun.boxmon.feature.payment.dto;

import lombok.Data;

@Data
public class ConfirmPaymentRequest {
    private String paymentKey;
    private Long shipmentId;
}
