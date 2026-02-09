package com.tjoeun.boxmon.feature.payment.controller;

import com.tjoeun.boxmon.feature.payment.domain.PaymentMethod;
import com.tjoeun.boxmon.feature.payment.dto.AuthKeyRequest;
import com.tjoeun.boxmon.feature.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // JWT 토큰에서 사용자 ID를 추출하는 임시 메서드
    // 실제 구현에서는 UserDetails 또는 CustomUserPrincipal 등을 사용해야 합니다.
    private Long getCurrentShipperId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.tjoeun.boxmon.feature.user.domain.User) {
            // 가정: Principal이 User 도메인 객체이며 getId() 메서드를 가지고 있음
            return ((com.tjoeun.boxmon.feature.user.domain.User) authentication.getPrincipal()).getUserId();
        }
        // 인증 정보가 없거나, 예상한 Principal 타입이 아닌 경우
        throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
    }

    // 결제 수단(빌링키) 등록 엔드포인트
    @PostMapping("/billingkey")
    public ResponseEntity<PaymentMethod> registerBillingKey(@RequestBody AuthKeyRequest authKeyRequest) {
        Long shipperId = getCurrentShipperId(); // 현재 로그인된 화주 ID 가져오기
        PaymentMethod paymentMethod = paymentService.registerBillingKey(shipperId, authKeyRequest.getAuthKey());
        return new ResponseEntity<>(paymentMethod, HttpStatus.CREATED);
    }
}