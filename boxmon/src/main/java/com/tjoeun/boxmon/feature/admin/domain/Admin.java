package com.tjoeun.boxmon.feature.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Setter
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name="is_delete")
    private Boolean isDelete;

    protected Admin(){

    }

    public Admin(String loginId, String password, String name, Boolean isDelete) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.isDelete = isDelete;
    }


}
