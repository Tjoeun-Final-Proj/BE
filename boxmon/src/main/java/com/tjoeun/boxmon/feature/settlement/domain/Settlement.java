package com.tjoeun.boxmon.feature.settlement.domain;

import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long settlementId;
    
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus;
    
    @Column(name = "settle_scheduled_at")
    private LocalDateTime settleScheduledAt;
    
    @Column(name = "settle_at")
    private LocalDateTime settledAt;
}
