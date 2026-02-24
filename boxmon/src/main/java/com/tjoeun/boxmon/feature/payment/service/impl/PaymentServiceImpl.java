package com.tjoeun.boxmon.feature.payment.service.impl;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import com.tjoeun.boxmon.feature.payment.repository.PaymentLogRepository;
import com.tjoeun.boxmon.feature.payment.client.TossApiClient;
import com.tjoeun.boxmon.feature.payment.domain.PaymentLog;
import com.tjoeun.boxmon.feature.payment.domain.PaymentEvent;
import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import com.tjoeun.boxmon.feature.payment.mapper.PaymentLogMapper;
import com.tjoeun.boxmon.feature.payment.service.PaymentCancelUseCase;
import com.tjoeun.boxmon.feature.payment.service.PaymentConfirmUseCase;
import com.tjoeun.boxmon.feature.shipment.domain.PaymentStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentConfirmUseCase {
    private final TossApiClient tossApiClient;
    private final ShipmentRepository shipmentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final PaymentLogMapper mapper;

    @Override
    public void confirmPayment(ConfirmPaymentRequest request) {
        //운송정보에서 화주가 지불해야하는 금액 확인
        Shipment shipment = shipmentRepository.findById(request.getShipmentId()).orElseThrow(()->new IllegalArgumentException("운송정보를 찾을 수 없습니다."));
        long longAmount = shipment.getPrice().setScale(0, RoundingMode.HALF_UP).longValueExact();
        
        //결제 승인 요청
        try {
            tossApiClient.confirmPayment(request.getPaymentKey(), String.format("%06d", request.getShipmentId()), longAmount);
        }
        catch (ExternalServiceException e) {
            //결제 승인 거절 로그 기록
            PaymentLog paymentLog = mapper.confirmRequestToPaymentLog(request, PaymentEvent.REJECTED);
            paymentLogRepository.save(paymentLog);
            throw e;
        }

        //결제 승인 로그 기록
        PaymentLog paymentLog = mapper.confirmRequestToPaymentLog(request, PaymentEvent.APPROVED);
        paymentLogRepository.save(paymentLog);
        
        //결제 승인 상태 기록
        shipment.setPaymentStatus(PaymentStatus.PAID);
        paymentLogRepository.save(paymentLog);
    }
}
