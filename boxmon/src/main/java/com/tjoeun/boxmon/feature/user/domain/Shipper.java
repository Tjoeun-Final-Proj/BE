package com.tjoeun.boxmon.feature.user.domain;

import com.tjoeun.boxmon.feature.user.domain.User;
import jakarta.persistence.*;

@Entity
public class Shipper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipperId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Shipper() {}

    public Shipper(User user) {
        this.user = user;
    }

    public Long getShipperId() {
        return shipperId;
    }

    public User getUser() {
        return user;
    }
}
