package com.tjoeun.boxmon.feature.payment.exception.handler;

import com.tjoeun.boxmon.feature.payment.exception.InvalidPaymentStatusException;
import com.tjoeun.boxmon.feature.payment.exception.PaymentConfirmConflictException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//결제 전용 예외처리기
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.tjoeun.boxmon.feature.payment")
public class PaymentExceptionController {
    //같은 결제에 대한 중복 승인 요청의 충돌처리 -> 409(conflict)
    @ExceptionHandler(PaymentConfirmConflictException.class)
    public ResponseEntity<String> paymentConfirmConflictException(PaymentConfirmConflictException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
    
    //결제 상태 전이 실패시 
    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<String> paymentConfirmConflictException(InvalidPaymentStatusException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
