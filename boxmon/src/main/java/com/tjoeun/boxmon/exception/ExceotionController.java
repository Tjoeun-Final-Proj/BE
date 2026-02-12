package com.tjoeun.boxmon.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceotionController {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handler1(UserNotFoundException e){
        return ResponseEntity.badRequest().body("이메일 다시 확인");
    }
}
