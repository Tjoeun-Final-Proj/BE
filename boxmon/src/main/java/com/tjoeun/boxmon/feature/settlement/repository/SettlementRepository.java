package com.tjoeun.boxmon.feature.settlement.repository;

import com.tjoeun.boxmon.feature.settlement.domain.Settlement;
import com.tjoeun.boxmon.feature.settlement.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByShipment_ShipmentId(Long shipmentId);

    @Query("select st from Settlement st " +
            "join fetch st.shipment s " +
            "where s.shipper.shipperId = :shipperId " +
            "and s.createdAt between :startDate and :endDate " +
            "and (:shipmentStatus is null or s.shipmentStatus = :shipmentStatus) " +
            "and (:settlementStatus is null or st.settlementStatus = :settlementStatus) " +
            "order by s.createdAt desc")
    List<Settlement> findShipperSettlementsForPeriod(
            @Param("shipperId") Long shipperId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("shipmentStatus") ShipmentStatus shipmentStatus,
            @Param("settlementStatus") SettlementStatus settlementStatus
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Settlement s " +
            "set s.settlementStatus = com.tjoeun.boxmon.feature.settlement.domain.SettlementStatus.PROGRESS " +
            "where s.shipment.shipmentId = :shipmentId " +
            "and s.settlementStatus = com.tjoeun.boxmon.feature.settlement.domain.SettlementStatus.PENDING")
    int saveSettlementProgressByShipmentId(@Param("shipmentId") Long shipmentId);
}
