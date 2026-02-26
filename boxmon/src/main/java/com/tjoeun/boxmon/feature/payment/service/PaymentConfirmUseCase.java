package com.tjoeun.boxmon.feature.payment.service;

import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;

public interface PaymentConfirmUseCase {
    void confirmPayment(ConfirmPaymentRequest request);
}
