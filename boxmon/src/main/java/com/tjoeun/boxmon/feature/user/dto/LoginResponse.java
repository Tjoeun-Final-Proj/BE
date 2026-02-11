package com.tjoeun.boxmon.feature.user.dto;

import com.tjoeun.boxmon.feature.user.domain.UserType;
import lombok.Getter;

@Getter
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserType userType;


    public LoginResponse(String accessToken, String refreshToken, UserType userType){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userType = userType;

    }

}

