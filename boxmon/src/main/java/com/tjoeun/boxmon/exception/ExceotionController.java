package com.tjoeun.boxmon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceotionController {
    // 사용자 미존재 에러를 400(Bad Request)으로 변환
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handler1(UserNotFoundException e){
        e.printStackTrace();
        return ResponseEntity.badRequest().body("요청한 사용자를 찾을 수 없습니다.");
    }

    // 잘못된 입력 파라미터를 400(Bad Request)으로 변환
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgumentHandler(IllegalArgumentException e){
        e.printStackTrace();
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 외부 연동 실패를 503(Service Unavailable)으로 변환
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<String> externalServiceException(ExternalServiceException e){
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("외부 API 연동에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    // 배송을 찾을 수 없을 때 404(Not Found)로 변환
    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<String> shipmentNotFoundException(ShipmentNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // 배송 상태 충돌(중복 배차/부적절한 상태) 시 409(Conflict)로 변환
    @ExceptionHandler(ShipmentStateConflictException.class)
    public ResponseEntity<String> shipmentStateConflictException(ShipmentStateConflictException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    // 권한이 없을 때 403(Forbidden)으로 변환
    @ExceptionHandler(RoleAccessDeniedException.class)
    public ResponseEntity<String> roleAccessDeniedException(RoleAccessDeniedException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
}
