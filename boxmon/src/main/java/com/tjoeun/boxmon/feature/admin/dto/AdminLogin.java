package com.tjoeun.boxmon.feature.admin.dto;

import lombok.Getter;

@Getter
public class AdminLogin {
    private String accessToken;
    private String refreshToken;

    public AdminLogin(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
