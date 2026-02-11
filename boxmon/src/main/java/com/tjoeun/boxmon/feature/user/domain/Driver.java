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

    private String bankCode;

    private String accountNumber;

    @Column(nullable = false)
    private String certNumber;

    @Column(name="holder_name")
    private String holderName;

    protected Driver() {}

    public Driver(User user, String certNumber) {
        this.user = user;
        this.certNumber = certNumber;
    }


}
