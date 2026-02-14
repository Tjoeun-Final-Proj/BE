package com.tjoeun.boxmon.feature.shipment.repository;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // 1. 특정 화주의 전체 화물을 최신 등록순으로 조회
    List<Shipment> findByShipper_ShipperIdOrderByCreatedAtDesc(Long shipperId);

    // 2. 특정 화주의 화물을 상태별 필터링하여 최신 등록순으로 조회
    List<Shipment> findByShipper_ShipperIdAndShipmentStatusOrderByCreatedAtDesc(Long shipperId, ShipmentStatus shipmentStatus);

    // 3. 특정 화주의 특정 기간 내 금액 합계를 가져오는 쿼리
    @Query("SELECT SUM(s.price) FROM Shipment s " +
            "WHERE s.shipper.shipperId = :shipperId " +
            "AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    BigDecimal findTotalAmountByShipperAndPeriod(
            @Param("shipperId") Long shipperId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 3. 특정 차주의 특정 기간 내 금액 합계를 가져오는 쿼리
    @Query("SELECT SUM(s.profit) FROM Shipment s " +
            "WHERE s.driver.driverId = :driverId " +
            "AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    BigDecimal findTotalProfitByDriverAndPeriod(
            @Param("driverId") Long driverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}