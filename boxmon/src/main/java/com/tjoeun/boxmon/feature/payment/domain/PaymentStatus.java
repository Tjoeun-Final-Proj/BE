package com.tjoeun.boxmon.feature.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PAID("결제 완료"), 
    UNPAID("미결제"),
    CANCEL_PROGRESS("결제 취소 처리중"),
    CANCELED("결제 취소 완료");
    
    private final String description;
}
