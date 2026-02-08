package com.tjoeun.boxmon.feature.user.domain;
import jakarta.persistence.*;

@Entity
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected Driver() {}

    public Driver(User user) {
        this.user = user;
        this.status = Status.PENDING;
    }
    public Status getApprovalStatus() {
        return status;
    }

    public void approve() {
        this.status = Status.APPROVED;
    }

    public void reject() {
        this.status = Status.REJECT;
    }

    public Status getStatus() {
        return status;
    }
}
