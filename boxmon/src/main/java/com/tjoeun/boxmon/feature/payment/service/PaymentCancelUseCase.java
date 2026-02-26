package com.tjoeun.boxmon.feature.payment.service;

public interface PaymentCancelUseCase {
    /**
     * 결제 취소 메소드
     * @param shipmentId 결제를 취소할 운송건
     * @param cancelReason 취소 사유(토스 api 호출에 필요, 정형화된 형식은 아니어도 됨)
     */
    void cancelPayment(Long shipmentId, String cancelReason);
}
