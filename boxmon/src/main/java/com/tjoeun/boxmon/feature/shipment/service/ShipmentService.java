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
     * 배차를 수락하여 요청 건에 배차 기사 정보를 부여하고 상태를 배차 완료로 변경합니다.
     *
     * @param driverId 배차 기사 ID
     * @param shipmentId 배차 대상 배송 ID
     */
    void acceptShipment(Long driverId, Long shipmentId);

    /**
     * 배차된 배송 건의 운송을 시작합니다.
     * ASSIGNED 상태만 IN_TRANSIT로 상태 전이를 허용합니다.
     *
     * @param driverId 운송 기사 ID
     * @param shipmentId 배송 ID
     */
    void startTransport(Long driverId, Long shipmentId);

    /**
     * 드라이버가 배차를 완료 처리합니다.
     * IN_TRANSIT 상태에서만 완료 처리할 수 있습니다.
     * 완료 시 하차 사진 URL(예: AWS S3 업로드 URL)을 저장합니다.
     *
     * @param driverId 배차된 드라이버 ID
     * @param shipmentId 배송 ID
     * @param dropoffPhotoUrl 하차 완료 사진 URL(미연결 상태에서는 null 허용)
     */
    void completeTransport(Long driverId, Long shipmentId, String dropoffPhotoUrl);

    /**
     * 특정 배송의 상세 정보를 조회합니다. 예상 도착 시간 및 실시간 위치 정보가 포함될 수 있습니다.
     * @param shipmentId 배송 고유 식별자
     * @return 배송 상세 응답 DTO
     */
    ShipmentDetailResponse getShipmentDetail(Long shipmentId);

    /**
     * 배차 수락용 상세 조회 (ETA/거리 계산 제외)
     * @param shipmentId 배송 ID
     * @return 배송 상세 응답 DTO
     */
    ShipmentDetailResponse getShipmentAcceptDetail(Long shipmentId);

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

