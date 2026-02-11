package com.tjoeun.boxmon.feature.user.dto;

import lombok.Getter;

@Getter
public class UserModify {
    private String name;
    private String phone;
    private String businessNumber;
    private Boolean isPushEnabled;

    public UserModify(String name, String phone, String businessNumber, Boolean isPushEnabled){
        this.name = name;
        this.phone = phone;
        this.businessNumber = businessNumber;
        this.isPushEnabled = isPushEnabled;
    }
}
