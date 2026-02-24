package com.tjoeun.boxmon.feature.shipment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PAID("결제완료"), 
    UNPAID("미결제");
    
    private final String description;
}
