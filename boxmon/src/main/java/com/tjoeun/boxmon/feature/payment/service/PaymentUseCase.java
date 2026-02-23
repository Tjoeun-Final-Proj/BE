package com.tjoeun.boxmon.feature.payment.service;

import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;

public interface PaymentUseCase {
    void confirmPayment(ConfirmPaymentRequest request);
}
