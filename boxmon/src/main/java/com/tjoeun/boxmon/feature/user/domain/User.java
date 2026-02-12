package com.tjoeun.boxmon.feature.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false, name = "is_push_enabled")
    private Boolean isPushEnabled;

    @Column(name = "business_number")
    private String businessNumber;

    @Column(nullable = false, name = "device_token")
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="user_type")
    protected UserType userType;

    protected User(){

    }

    //회원가입 형식
    public User(String email, String password, String name, String phone, LocalDate birth, Boolean isPushEnabled, UserType userType, String businessNumber, String deviceToken){
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
        this.birth = birth;
        this.isPushEnabled = isPushEnabled;
        this.userType = userType;
        this.businessNumber = businessNumber;
        this.deviceToken = deviceToken;
    }

}
