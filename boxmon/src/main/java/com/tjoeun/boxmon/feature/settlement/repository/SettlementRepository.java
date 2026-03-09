package com.tjoeun.boxmon.feature.settlement.repository;

import com.tjoeun.boxmon.feature.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByShipment_ShipmentId(Long shipmentId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Settlement s " +
            "set s.settlementStatus = com.tjoeun.boxmon.feature.settlement.domain.SettlementStatus.PROGRESS " +
            "where s.shipment.shipmentId = :shipmentId " +
            "and s.settlementStatus = com.tjoeun.boxmon.feature.settlement.domain.SettlementStatus.PENDING")
    int saveSettlementProgressByShipmentId(@Param("shipmentId") Long shipmentId);
}
