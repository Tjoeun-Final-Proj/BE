package com.tjoeun.boxmon.feature.user.domain;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @Column(name="bank_code")
    private String bankCode;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "holder_name")
    private String holderName;
    
    @Column(name = "toss_seller_id")
    private String tossSellerId;

    protected Driver() {}

    public Driver(User user) {
        this.user = user;
    }
}
