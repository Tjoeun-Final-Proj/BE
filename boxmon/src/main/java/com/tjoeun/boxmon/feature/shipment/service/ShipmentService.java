package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 배송(Shipment) 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 배송 생성, 상세 조회, 화주 및 운송 기사별 정산 요약 및 목록 조회 기능을 제공합니다.
 */
public interface ShipmentService {
    /**
     * 화물 운송 요청을 등록합니다.
     *
     * @param shipperId 화주 ID
     * @param request 배송 생성 요청 DTO
     */
    void createShipment(Long shipperId, ShipmentCreateRequest request);

    /**
     * 특정 배송의 상세 정보를 조회합니다. 예상 도착 시간 및 실시간 위치 정보가 포함될 수 있습니다.
     * @param shipmentId 배송 고유 식별자
     * @return 배송 상세 응답 DTO
     */
    ShipmentDetailResponse getShipmentDetail(Long shipmentId);

    List<UnassignedShipmentResponse> getUnassignedShipments();

    /**
     * 특정 화주의 정산 요약 정보를 조회합니다.
     * @param shipperId 화주 ID
     * @return 화주 정산 요약 응답 DTO
     */
    ShipperSettlementSummaryResponse getShipperSettlementSummary(Long shipperId);

    /**
     * 특정 운송 기사의 정산 요약 정보를 조회합니다.
     * @param driverId 운송 기사 ID
     * @return 운송 기사 정산 요약 응답 DTO
     */
    DriverSettlementSummaryResponse getDriverSettlementSummary(Long driverId);

    /**
     * 특정 화주의 정산 목록을 조회합니다.
     * @param shipperId 화주 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param shipmentStatus (선택 사항) 배송 상태 필터
     * @param settlementStatus (선택 사항) 정산 상태 필터
     * @return 화주 정산 목록 응답 DTO 리스트
     */
    List<ShipperSettlementListResponse> getShipperSettlementList(
            Long shipperId,
            int year,
            int month,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    );

    /**
     * 특정 운송 기사의 정산 목록을 조회합니다.
     * @param driverId 운송 기사 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param shipmentStatus (선택 사항) 배송 상태 필터
     * @param settlementStatus (선택 사항) 정산 상태 필터
     * @return 운송 기사 정산 목록 응답 DTO 리스트
     */
    List<DriverSettlementListResponse> getDriverSettlementList(
            Long driverId,
            int year,
            int month,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    );
}
