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
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false)
    private Boolean isPushEnabled;

    private String businessNumber;

    @Column(nullable = false)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
