package com.tjoeun.boxmon.feature.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentEvent {
    APPROVED("결제 승인"), 
    REJECTED("결제 승인 거절"), 
    CANCELED("결제 취소"), 
    CANCEL_FAILED("결제 취소 거절");
    
    private final String description;
}
