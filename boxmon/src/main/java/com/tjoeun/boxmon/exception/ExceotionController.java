package com.tjoeun.boxmon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceotionController {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handler1(UserNotFoundException e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("?대찓???ㅼ떆 ?뺤씤");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgumentHandler(IllegalArgumentException e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<String> externalServiceException(ExternalServiceException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("?쒕쾭媛 ?몃?????듭떊???ㅽ뙣?덉뒿?덈떎. 愿由ъ옄?먭쾶 臾몄쓽?댁＜?몄슂.");
    }

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<String> shipmentNotFoundException(ShipmentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ShipmentStateConflictException.class)
    public ResponseEntity<String> shipmentStateConflictException(ShipmentStateConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(RoleAccessDeniedException.class)
    public ResponseEntity<String> roleAccessDeniedException(RoleAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
}
