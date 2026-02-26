package com.tjoeun.boxmon.feature.payment.repository;

import com.tjoeun.boxmon.feature.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByShipment_ShipmentId(Long shipmentId);
  
    //조건부 update: 결제 완료 상태일 경우에만 취소 진행중으로 상태 전이
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Payment p " +
            "set p.paymentStatus = com.tjoeun.boxmon.feature.payment.domain.PaymentStatus.CANCEL_PROGRESS " +
            "where p.paymentId = :paymentId " +
            "and p.paymentStatus = com.tjoeun.boxmon.feature.payment.domain.PaymentStatus.PAID")
    int savePaymentCancelProgress(@Param("paymentId") Long paymentId);
}
