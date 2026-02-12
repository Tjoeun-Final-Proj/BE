package com.tjoeun.boxmon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@Slf4j
@RestControllerAdvice
public class ExceotionController {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handler1(UserNotFoundException e){
        return ResponseEntity.badRequest().body("이메일 다시 확인");
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgumentHandler(IllegalArgumentException e){
        e.printStackTrace();
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<String> externalServiceException(ExternalServiceException e){
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("서버가 외부와의 통신에 실패했습니다. 관리자에게 문의해주세요.");
    }
}
