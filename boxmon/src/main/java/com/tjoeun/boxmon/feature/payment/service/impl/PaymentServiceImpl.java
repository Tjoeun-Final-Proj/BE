package com.tjoeun.boxmon.feature.payment.service.impl;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import com.tjoeun.boxmon.feature.payment.exception.PaymentConfirmConflictException;
import com.tjoeun.boxmon.feature.payment.domain.Payment;
import com.tjoeun.boxmon.feature.payment.repository.PaymentLogRepository;
import com.tjoeun.boxmon.global.client.TossApiClient;
import com.tjoeun.boxmon.feature.payment.domain.PaymentLog;
import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import com.tjoeun.boxmon.feature.payment.mapper.PaymentMapper;
import com.tjoeun.boxmon.feature.payment.repository.PaymentRepository;
import com.tjoeun.boxmon.feature.payment.service.PaymentCancelUseCase;
import com.tjoeun.boxmon.feature.payment.service.PaymentConfirmUseCase;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentConfirmUseCase, PaymentCancelUseCase {
    private final TossApiClient tossApiClient;
    private final ShipmentRepository shipmentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final PaymentMapper mapper;
    private final PaymentRepository paymentRepository;

    //결제 승인
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
    
    //결제 취소
    @Override
    @Transactional
    public void cancelPayment(Long shipmentId, String cancelReason) {
        //취소할 결제 정보 확인
        Payment payment = paymentRepository.findByShipment_ShipmentId(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("취소할 결제정보를 찾을 수 없습니다."));

        //결제 취소중 상태를 DB에 기록
        int updatedRow = paymentRepository.savePaymentCancelProgress(payment.getPaymentId());
        if(updatedRow != 1){ //동시 요청 문제 해결
            log.warn("결제 취소 상태 전이 실패. 다른 스레드에서 이미 결제 취소를 진행중인것 같습니다. 결제 취소 요청을 무시하고 즉시 성공으로 응답합니다.");
            return;
        }
        
        //결제 취소 요청
        try {
            tossApiClient.cancelPayment(payment.getTossPaymentKey(), cancelReason);
        }
        catch (ExternalServiceException e) {
            //결제 취소 거절 로그 기록
            PaymentLog paymentLog = mapper.logCancelFailed(payment);
            paymentLogRepository.save(paymentLog);
            
            //결제 완료 상태로 복귀
            payment.rollbackCancel();
            paymentRepository.save(payment);
        }

        //결제 취소 로그 기록
        PaymentLog paymentLog = mapper.logCanceled(payment);
        paymentLogRepository.save(paymentLog);
        
        //결제 상태 갱신
        payment.cancel();
        paymentRepository.save(payment);
    }
}
