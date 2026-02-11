package com.tjoeun.boxmon.feature.user.dto;

import com.tjoeun.boxmon.feature.user.domain.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DriverSignupRequest {

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

    @NotNull
    private String bankCode;

    @NotNull
    private String accountNumber;

    @NotNull
    private String holderName;

    @NotNull
    private String certNumber;

}
