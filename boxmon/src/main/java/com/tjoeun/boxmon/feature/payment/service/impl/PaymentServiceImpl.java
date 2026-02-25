package com.tjoeun.boxmon.feature.payment.service.impl;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import com.tjoeun.boxmon.exception.PaymentConfirmConflictException;
import com.tjoeun.boxmon.feature.payment.domain.Payment;
import com.tjoeun.boxmon.feature.payment.repository.PaymentLogRepository;
import com.tjoeun.boxmon.feature.payment.client.TossApiClient;
import com.tjoeun.boxmon.feature.payment.domain.PaymentLog;
import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import com.tjoeun.boxmon.feature.payment.mapper.PaymentMapper;
import com.tjoeun.boxmon.feature.payment.repository.PaymentRepository;
import com.tjoeun.boxmon.feature.payment.service.PaymentCancelUseCase;
import com.tjoeun.boxmon.feature.payment.service.PaymentConfirmUseCase;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentConfirmUseCase, PaymentCancelUseCase {
    private final TossApiClient tossApiClient;
    private final ShipmentRepository shipmentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final PaymentMapper mapper;
    private final PaymentRepository paymentRepository;

    @Override
    public void confirmPayment(ConfirmPaymentRequest request) {
        //결제 대기 상태 생성
        Payment payment = mapper.confirmRequestToPayment(request);
        try {
            payment = paymentRepository.save(payment);            
        }
        catch (DataIntegrityViolationException e) { //이미 처리된 결제에 대한 충돌 처리
            throw new PaymentConfirmConflictException("이미 승인된 결제입니다.");
        }
        
        //운송정보에서 화주가 지불해야하는 금액 확인
        Shipment shipment = shipmentRepository.findById(request.getShipmentId()).orElseThrow(()->new IllegalArgumentException("운송정보를 찾을 수 없습니다."));
        long longAmount = shipment.getPrice().setScale(0, RoundingMode.HALF_UP).longValueExact();
        
        //결제 승인 요청
        try {
            tossApiClient.confirmPayment(request.getPaymentKey(), String.format("%06d", request.getShipmentId()), longAmount);
        }
        catch (ExternalServiceException e) {
            //결제 승인 거절 로그 기록
            PaymentLog paymentLog = mapper.logConfirmRejected(payment);
            paymentLogRepository.save(paymentLog);
            throw e;
        }

        //결제 승인 로그 기록
        PaymentLog paymentLog = mapper.logConfirmApproved(payment);
        paymentLogRepository.save(paymentLog);
        
        //결제 승인 상태 갱신
        payment.approve();
        paymentRepository.save(payment);
    }
    
    @Override
    @Transactional
    public void cancelPayment(Long shipmentId) {
        //TODO 토스 api를 이용한 취소 처리
        
        
        Payment payment = paymentRepository.findByShipment_ShipmentId(shipmentId).orElseThrow(() -> new IllegalArgumentException("취소할 결제정보를 찾을 수 없습니다."));
        //TODO 조건부 update 처리(PAID일때만 UNPAID로 변경하는 jpql 사용 & 리턴값이 1이 아니면 log.warn으로 경고)
    }
}
