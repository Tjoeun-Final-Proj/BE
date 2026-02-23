package com.tjoeun.boxmon.feature.payment.domain;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PaymentLog {
    @Id
    private String paymentKey;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Setter
    private PaymentStatus status;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
