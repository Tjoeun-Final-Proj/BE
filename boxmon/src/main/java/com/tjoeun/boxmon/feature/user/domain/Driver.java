package com.tjoeun.boxmon.feature.user.domain;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Driver {

    @Id
    @Column(name="driver_id")
    private Long driverId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String bankCode;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String certNumber;

    @Column(nullable = false)
    private String holderName;

    protected Driver() {}

    public Driver(User user, String bankCode, String accountNumber, String certNumber, String holderName) {
        this.user = user;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.certNumber = certNumber;
        this.holderName = holderName;
    }

}
