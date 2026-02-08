package com.tjoeun.boxmon.feature.user.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected UserType userType;

    protected User(){

    }

    //회원가입 형식
    public User(String email, String password, String name, String phone, LocalDate birth, UserType userType){
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
        this.birth = birth;
        this.isPushEnabled = true;
        this.userType = userType;
    }


    public String getPassword() {
        return password;
    }

    public Long getUserId() {
        return userId;
    }
}
