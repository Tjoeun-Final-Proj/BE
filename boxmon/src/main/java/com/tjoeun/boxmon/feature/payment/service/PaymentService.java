package com.tjoeun.boxmon.feature.payment.service;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import com.tjoeun.boxmon.feature.payment.PaymentLogRepository;
import com.tjoeun.boxmon.feature.payment.client.TossApiClient;
import com.tjoeun.boxmon.feature.payment.domain.PaymentLog;
import com.tjoeun.boxmon.feature.payment.domain.PaymentStatus;
import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import com.tjoeun.boxmon.feature.payment.mapper.PaymentLogMapper;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final TossApiClient tossApiClient;
    private final ShipmentRepository shipmentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final PaymentLogMapper mapper;

    public void confirmPayment(ConfirmPaymentRequest request) {
        //요청 로그 기록
        PaymentLog paymentLog = mapper.confirmRequestToPaymentLog(request);
        paymentLog = paymentLogRepository.save(paymentLog);
        
        Shipment shipment = shipmentRepository.findById(request.getShipmentId()).orElseThrow(()->new IllegalArgumentException("운송정보를 찾을 수 없습니다."));
        long longAmount = shipment.getPrice().setScale(0, RoundingMode.HALF_UP).longValueExact();
        try {
            tossApiClient.confirmPayment(request.getPaymentKey(), String.format("%06d", request.getShipmentId()), longAmount);
            paymentLog.setStatus(PaymentStatus.APPROVED);
            paymentLogRepository.save(paymentLog);
        }
        catch (ExternalServiceException e) {
            paymentLog.setStatus(PaymentStatus.REJECTED);
            paymentLogRepository.save(paymentLog);
            throw e;
        }
    }
}
