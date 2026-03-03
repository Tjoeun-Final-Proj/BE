package com.tjoeun.boxmon.feature.settlement.repository;

import com.tjoeun.boxmon.feature.settlement.domain.Settlement;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByShipment(Shipment shipment);
}
