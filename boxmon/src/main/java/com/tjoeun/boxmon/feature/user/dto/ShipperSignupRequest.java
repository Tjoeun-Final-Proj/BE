package com.tjoeun.boxmon.feature.user.dto;

import com.tjoeun.boxmon.feature.user.domain.UserType;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ShipperSignupRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotNull
    private LocalDate birth;

    @NotNull
    private Boolean isPushEnabled;

    @NotNull
    private UserType userType;

    private String businessNumber;

    public ShipperSignupRequest(String email, String password, String name, String phone, LocalDate birth, Boolean isPushEnabled, UserType userType, String businessNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birth = birth;
        this.isPushEnabled = isPushEnabled;
        this.userType = userType;
        this.businessNumber = businessNumber;
    }


}
