package com.tjoeun.boxmon.feature.user.dto;

import com.tjoeun.boxmon.feature.user.domain.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DriverDetail {

    private String email;

    private String name;

    private String phone;

    private LocalDateTime createdAt;

    private LocalDate birth;

    private Boolean isPushEnabled;

    private String businessNumber;

    private Boolean accountStatus;

    private String bankCode;

    private String accountNumber;

    private String holderName;

    private String vehicleNumber;

    private VehicleType vehicleType;

    private Boolean canRefrigerate;

    private Boolean canFreeze;

    private Double weightCapacity;




}
