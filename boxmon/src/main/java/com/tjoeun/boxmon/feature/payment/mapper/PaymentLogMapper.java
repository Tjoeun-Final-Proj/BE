package com.tjoeun.boxmon.feature.payment.mapper;

import com.tjoeun.boxmon.feature.payment.domain.PaymentLog;
import com.tjoeun.boxmon.feature.payment.domain.PaymentStatus;
import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentLogMapper {
    private final EntityManager em;

    public PaymentLog confirmRequestToPaymentLog(ConfirmPaymentRequest request){
        Shipment shipment = em.getReference(Shipment.class,request.getShipmentId());
        return PaymentLog.builder()
                .paymentKey(request.getPaymentKey())
                .shipment(shipment)
                .status(PaymentStatus.PENDING)
                .updateTime(LocalDateTime.now())
                .build();
    }
}
