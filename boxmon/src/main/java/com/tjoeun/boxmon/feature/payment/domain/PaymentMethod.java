package com.tjoeun.boxmon.feature.payment.domain;

import com.tjoeun.boxmon.feature.user.domain.Shipper;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PAYMENT_METHOD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id")
    private Long paymentMethodId; // 결제수단 식별자

    // 화주와의 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private Shipper shipper;

    // 토스 빌링키 발급 API로 받아야 함
    @Column(name = "billing_key", nullable = false, length = 255)
    private String billingKey;
}