package com.tjoeun.boxmon.feature.payment.service;

import com.tjoeun.boxmon.feature.payment.client.TossApiClient;
import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final ShipperRepository shipperRepository;
    private final TossApiClient tossApiClient;
    private final ShipmentRepository shipmentRepository;

    public void confirmPayment(ConfirmPaymentRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getShipmentId()).orElseThrow(()->new IllegalArgumentException("운송정보를 찾을 수 없습니다."));
        long longAmount = shipment.getPrice().setScale(0, RoundingMode.HALF_UP).longValueExact();
        tossApiClient.confirmPayment(request.getPaymentKey(), String.format("%06d",request.getShipmentId()), longAmount);
    }
}
