package com.tjoeun.boxmon.feature.payment.controller;

import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import com.tjoeun.boxmon.feature.payment.service.PaymentService;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final ShipmentRepository shipmentRepository;

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment(@RequestBody ConfirmPaymentRequest request) {
        log.info("request={}",request);
        paymentService.confirmPayment(request);
        throw new NotImplementedException("not yet implemented"); //TODO 서비스 구현이 끝나면 삭제해야함
    }
}