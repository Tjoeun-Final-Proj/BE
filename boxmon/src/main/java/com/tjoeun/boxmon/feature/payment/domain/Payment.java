package com.tjoeun.boxmon.feature.payment.domain;

import com.tjoeun.boxmon.feature.shipment.domain.PaymentStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_payment_shipment",
                columnNames = {"shipment_id"}
        ),
        @UniqueConstraint(
                name = "uq_payment_toss_payment_key",
                columnNames = {"toss_payment_key"}
        ),
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Column(name = "toss_payment_key", nullable = false)
    private String tossPaymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    
    @Builder
    private Payment(Shipment shipment, String tossPaymentKey, PaymentStatus paymentStatus, LocalDateTime approvedAt, LocalDateTime canceledAt) {
        this.shipment = shipment;
        this.tossPaymentKey = tossPaymentKey;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.UNPAID;
        this.approvedAt = approvedAt;
        this.canceledAt = canceledAt;
    }
    
    public void approve() throws IllegalStateException {
        if(paymentStatus.equals(PaymentStatus.PAID))
            throw new IllegalStateException("이미 결제가 승인되었습니다.");
        paymentStatus = PaymentStatus.PAID;
        approvedAt = LocalDateTime.now();
    }
    
    public void cancel() throws IllegalStateException {
        if(paymentStatus.equals(PaymentStatus.UNPAID))
            throw new IllegalStateException("이미 결제가 취소 되었거나 아직 결제가 승인되지 않았습니다.");
        paymentStatus = PaymentStatus.UNPAID;
        canceledAt = LocalDateTime.now();
    }
}
