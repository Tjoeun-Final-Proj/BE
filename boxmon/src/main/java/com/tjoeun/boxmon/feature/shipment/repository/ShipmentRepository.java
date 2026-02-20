package com.tjoeun.boxmon.feature.shipment.repository;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Shipment 엔티티에 대한 데이터 접근(Repository) 인터페이스입니다.
 * Spring Data JPA의 JpaRepository를 상속받아 기본적인 CRUD 기능을 제공하며,
 * 화주 및 운송 기사별 배송 조회, 정산 정보 조회 등을 위한 사용자 정의 쿼리 메서드를 포함합니다.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {


    /**
     * 특정 화주의 특정 기간 내 모든 배송 목록을 생성일자 내림차순으로 조회합니다.
     *
     * @param shipperId 화주 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @return 조건에 해당하는 배송 목록
     */
    List<Shipment> findByShipper_ShipperIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long shipperId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 특정 화주의 특정 기간 내 지정된 배송 상태의 배송 목록을 생성일자 내림차순으로 조회합니다.
     *
     * @param shipperId 화주 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @param shipmentStatus 배송 상태
     * @return 조건에 해당하는 배송 목록
     */
    List<Shipment> findByShipper_ShipperIdAndCreatedAtBetweenAndShipmentStatusOrderByCreatedAtDesc(
            Long shipperId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ShipmentStatus shipmentStatus
    );

    /**
     * 특정 화주의 특정 기간 내 지정된 정산 상태의 배송 목록을 생성일자 내림차순으로 조회합니다.
     *
     * @param shipperId 화주 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @param settlementStatus 정산 상태
     * @return 조건에 해당하는 배송 목록
     */
    List<Shipment> findByShipper_ShipperIdAndCreatedAtBetweenAndSettlementStatusOrderByCreatedAtDesc(
            Long shipperId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            SettlementStatus settlementStatus
    );

    /**
     * 특정 화주의 특정 기간 내 지정된 배송 상태와 정산 상태의 배송 목록을 생성일자 내림차순으로 조회합니다.
     *
     * @param shipperId 화주 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @param shipmentStatus 배송 상태
     * @param settlementStatus 정산 상태
     * @return 조건에 해당하는 배송 목록
     */
    List<Shipment> findByShipper_ShipperIdAndCreatedAtBetweenAndShipmentStatusAndSettlementStatusOrderByCreatedAtDesc(
            Long shipperId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    );

    /**
     * 특정 운송 기사의 특정 기간 내 모든 배송 목록을 생성일자 내림차순으로 조회합니다.
     *
     * @param driverId 운송 기사 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @return 조건에 해당하는 배송 목록
     */
    List<Shipment> findByDriver_DriverIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long driverId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 특정 운송 기사의 특정 기간 내 지정된 배송 상태의 배송 목록을 생성일자 내림차순으로 조회합니다.
     *
     * @param driverId 운송 기사 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @param shipmentStatus 배송 상태
     * @return 조건에 해당하는 배송 목록
     */
    List<Shipment> findByDriver_DriverIdAndCreatedAtBetweenAndShipmentStatusOrderByCreatedAtDesc(
            Long driverId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ShipmentStatus shipmentStatus
    );

    /**
     * 특정 운송 기사의 특정 기간 내 지정된 정산 상태의 배송 목록을 생성일자 내림차순으로 조회합니다.
     *
     * @param driverId 운송 기사 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @param settlementStatus 정산 상태
     * @return 조건에 해당하는 배송 목록
     */
    List<Shipment> findByDriver_DriverIdAndCreatedAtBetweenAndSettlementStatusOrderByCreatedAtDesc(
            Long driverId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            SettlementStatus settlementStatus
    );

    /**
     * 특정 운송 기사의 특정 기간 내 지정된 배송 상태와 정산 상태의 배송 목록을 생성일자 내림차순으로 조회합니다.
     *
     * @param driverId 운송 기사 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @param shipmentStatus 배송 상태
     * @param settlementStatus 정산 상태
     * @return 조건에 해당하는 배송 목록
     */
    List<Shipment> findByDriver_DriverIdAndCreatedAtBetweenAndShipmentStatusAndSettlementStatusOrderByCreatedAtDesc(
            Long driverId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    );

    /**
     * 특정 화주의 특정 기간 내 모든 배송의 총 운임 합계를 조회합니다.
     *
     * @param shipperId 화주 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @return 해당 기간 내 총 운임 합계
     */
    @Query("SELECT SUM(s.price) FROM Shipment s " +
            "WHERE s.shipper.shipperId = :shipperId " +
            "AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    BigDecimal findTotalAmountByShipperAndPeriod(
            @Param("shipperId") Long shipperId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 운송 기사의 특정 기간 내 모든 배송의 총 수익 합계를 조회합니다.
     *
     * @param driverId 운송 기사 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @return 해당 기간 내 총 수익 합계
     */
    @Query("SELECT SUM(s.profit) FROM Shipment s " +
            "WHERE s.driver.driverId = :driverId " +
            "AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    BigDecimal findTotalProfitByDriverAndPeriod(
            @Param("driverId") Long driverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Shipment> findByShipmentStatusOrderByCreatedAtDesc(ShipmentStatus shipmentStatus);
}
