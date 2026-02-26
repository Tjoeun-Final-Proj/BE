package com.tjoeun.boxmon.feature.payment.mapper;

import com.tjoeun.boxmon.feature.payment.domain.Payment;
import com.tjoeun.boxmon.feature.payment.domain.PaymentLog;
import com.tjoeun.boxmon.feature.payment.domain.PaymentEvent;
import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import com.tjoeun.boxmon.feature.payment.domain.PaymentStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentMapper {
    private final EntityManager em;
    
    public Payment confirmRequestToPayment(ConfirmPaymentRequest request) {
        Shipment shipment = em.getReference(Shipment.class,request.getShipmentId());
        return Payment.builder()
                .shipment(shipment)
                .tossPaymentKey(request.getPaymentKey())
                .paymentStatus(PaymentStatus.UNPAID)
                .approvedAt(null)
                .canceledAt(null)
                .build();
    }
    
    public PaymentLog logConfirmApproved(Payment payment) {
        return PaymentLog.builder()
                .payment(payment)
                .eventType(PaymentEvent.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
                
    }
    public PaymentLog logConfirmRejected(Payment payment) {
        return PaymentLog.builder()
                .payment(payment)
                .eventType(PaymentEvent.REJECTED)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public PaymentLog logCanceled(Payment payment) {
        return PaymentLog.builder()
                .payment(payment)
                .eventType(PaymentEvent.CANCELED)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public PaymentLog logCancelFailed(Payment payment) {
        return PaymentLog.builder()
                .payment(payment)
                .eventType(PaymentEvent.CANCEL_FAILED)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
