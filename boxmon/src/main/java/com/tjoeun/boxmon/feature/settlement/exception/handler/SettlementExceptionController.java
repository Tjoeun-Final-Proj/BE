package com.tjoeun.boxmon.feature.settlement.exception.handler;

import com.tjoeun.boxmon.feature.settlement.exception.SettlementConflictException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.tjoeun.boxmon.feature.settlement")
public class SettlementExceptionController {
    @ExceptionHandler(SettlementConflictException.class)
    public ResponseEntity<String> handleSettlementConflictException(SettlementConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
