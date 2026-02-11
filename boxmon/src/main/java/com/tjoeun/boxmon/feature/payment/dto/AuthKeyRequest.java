package com.tjoeun.boxmon.feature.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트엔드로부터 authKey를 받기 위한 요청 DTO
@Getter
@Setter
@NoArgsConstructor
public class AuthKeyRequest {
    private String authKey; // 토스페이먼츠에서 받은 인증키
}
