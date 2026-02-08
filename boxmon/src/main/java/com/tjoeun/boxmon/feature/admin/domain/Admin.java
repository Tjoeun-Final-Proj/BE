package com.tjoeun.boxmon.feature.admin.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;

@Entity
public class Admin {

    @jakarta.persistence.Id
    private Long id;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    protected Admin(){

    }

    public Admin(String loginId, String password, String name) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
    }
}
