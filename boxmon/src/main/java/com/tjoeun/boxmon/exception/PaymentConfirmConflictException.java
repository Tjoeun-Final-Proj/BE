package com.tjoeun.boxmon.exception;

public class PaymentConfirmConflictException extends RuntimeException {
    public PaymentConfirmConflictException(String message) {
        super(message);
    }
}
