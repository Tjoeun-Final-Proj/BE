package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ShipmentService {
    /**
     * 화물 운송 요청 등록
     */
    void createShipment(Long shipperId, ShipmentCreateRequest request);

    /**
     * 화주 본인의 화물 목록 조회
     */
    List<ShipmentListResponse> getMyShipmentList(Long shipperId, ShipmentStatus status);

    /**
     * 화물 상세 정보 조회 (ETA 및 실시간 위치 포함)
     * @param shipmentId 화물 식별자
     * @return 화물 상세 응답 DTO
     */
    ShipmentDetailResponse getShipmentDetail(Long shipmentId);

    ShipperSettlementSummaryResponse getShipperSettlementSummary(Long shipperId);

    DriverSettlementSummaryResponse getDriverSettlementSummary(Long driverId);
}