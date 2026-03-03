package com.tjoeun.boxmon.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ShipperDetail {

    private String email;

    private String name;

    private String phone;

    private LocalDateTime createdAt;

    private LocalDate birth;

    private Boolean isPushEnabled;

    private String businessNumber;

    private Boolean accountStatus;
}
