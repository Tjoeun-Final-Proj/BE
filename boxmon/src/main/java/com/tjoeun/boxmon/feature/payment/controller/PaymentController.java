package com.tjoeun.boxmon.feature.payment.controller;

import com.tjoeun.boxmon.feature.payment.domain.PaymentMethod;
import com.tjoeun.boxmon.feature.payment.dto.AuthKeyRequest;
import com.tjoeun.boxmon.feature.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    /*
    // JWT 토큰에서 사용자 ID를 추출하는 임시 메서드
    // 실제 구현에서는 UserDetails 또는 CustomUserPrincipal 등을 사용해야 합니다.
    private Long getCurrentShipperId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. 인증 객체가 null인지 확인
        // 2. Principal이 Long 타입인지 확인 (JwtFilter에서 Long으로 저장했기 때문)
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }

        // 디버깅을 위해 실제 들어있는 타입이 뭔지 로그를 찍어보는 것이 좋습니다.
        if (authentication != null) {
            System.out.println("Principal Type: " + authentication.getPrincipal().getClass().getName());
            System.out.println("Principal Value: " + authentication.getPrincipal());
        }

        throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
    }
     */

    // 결제 수단(빌링키) 등록 엔드포인트
    @PostMapping("/billingkey")
    public ResponseEntity<String> registerBillingKey(
            @AuthenticationPrincipal Long shipperId, // 이렇게 바로 받을 수 있습니다.
            @RequestBody AuthKeyRequest authKeyRequest
    ) {
        if (shipperId == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        try {
            paymentService.registerBillingKey(shipperId, authKeyRequest.getAuthKey());
            return ResponseEntity.status(HttpStatus.CREATED).body("결제수단이 성공적으로 등록되었습니다.");
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}