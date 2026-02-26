package com.tjoeun.boxmon.feature.payment.exception;

public class PaymentConfirmConflictException extends RuntimeException {
    public PaymentConfirmConflictException(String message) {
        super(message);
    }
}
