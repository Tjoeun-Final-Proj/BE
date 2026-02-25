package com.tjoeun.boxmon.feature.payment.repository;

import com.tjoeun.boxmon.feature.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByShipment_ShipmentId(Long shipmentId);
  
    //TODO 조건부 update 쿼리 완성
//    @Modifying(flushAutomatically = true, clearAutomatically = true)
//    @Query("update Payment p " +
//            "set p.paymentStatus")
}
