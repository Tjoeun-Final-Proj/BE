package com.tjoeun.boxmon.feature.admin.domain;

import com.tjoeun.boxmon.feature.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Penalties {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="penalty_id")
    private Long penaltyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name="admin_id")
    private Admin adminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name="user_id")
    private User userId;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    public Penalties( Admin adminId, User userId, String payload){
        this.adminId = adminId;
        this.userId = userId;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }

    protected Penalties() {

    }
}
