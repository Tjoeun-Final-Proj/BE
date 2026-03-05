package com.tjoeun.boxmon.feature.settlement.domain;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_settlement_shipment",
                columnNames = {"shipment_id"}
        )
})
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long settlementId;
    
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus;
    
    @Getter
    @Column(name = "settle_scheduled_at", nullable = false)
    private LocalDateTime settleScheduledAt;
    
    @Column(name = "settle_at")
    private LocalDateTime settledAt;
    
    @Column(name = "policy_version", nullable = false)
    private String policyVersion;
    
    @Getter
    @Setter
    @Column(name = "last_check_at")
    private LocalDateTime lastCheckAt;

    @Builder
    public Settlement(Shipment shipment, SettlementStatus settlementStatus, LocalDateTime settleScheduledAt, String policyVersion) {
        this.shipment = shipment;
        this.settlementStatus = settlementStatus;
        this.settleScheduledAt = settleScheduledAt;
        this.policyVersion = policyVersion;
    }
}
