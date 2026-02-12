package com.tjoeun.boxmon.feature.shipment.repository;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // 1. 특정 화주의 전체 화물을 최신 등록순으로 조회
    List<Shipment> findByShipper_ShipperIdOrderByCreatedAtDesc(Long shipperId);

    // 2. 특정 화주의 화물을 상태별 필터링하여 최신 등록순으로 조회
    List<Shipment> findByShipper_ShipperIdAndShipmentStatusOrderByCreatedAtDesc(Long shipperId, ShipmentStatus shipmentStatus);
}