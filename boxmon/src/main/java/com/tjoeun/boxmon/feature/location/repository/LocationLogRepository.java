package com.tjoeun.boxmon.feature.location.repository;

import com.tjoeun.boxmon.feature.location.domain.LocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {
    List<LocationLog> findByShipment_ShipmentIdOrderByCreatedAtAscLocationIdAsc(Long shipmentId);

    List<LocationLog> findByShipment_ShipmentIdAndCreatedAtBetweenOrderByCreatedAtAscLocationIdAsc(
            Long shipmentId,
            LocalDateTime from,
            LocalDateTime to
    );
}
