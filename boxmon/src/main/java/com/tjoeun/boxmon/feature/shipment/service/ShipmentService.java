package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentListResponse;

import java.util.List;

public interface ShipmentService {
    /**
     * 화물 운송 요청 등록
     */
    void createShipment(Long shipperId, ShipmentCreateRequest request);

    /**
     * 화주 본인의 화물 목록 조회 (상태별 필터링 포함)
     * @param shipperId 화주 식별자
     * @param status 필터링할 배송 상태 (null일 경우 전체 조회)
     * @return 화물 목록 응답 DTO 리스트
     */
    List<ShipmentListResponse> getMyShipmentList(Long shipperId, ShipmentStatus status);
}