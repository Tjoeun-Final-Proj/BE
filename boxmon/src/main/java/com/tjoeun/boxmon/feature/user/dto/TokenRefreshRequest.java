package com.tjoeun.boxmon.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenRefreshRequest {

    @NotBlank
    private String refreshToken;

}
