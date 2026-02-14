package com.tjoeun.boxmon.feature.shipment.controller;

import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.*;
import com.tjoeun.boxmon.feature.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createShipment(
            Authentication authentication,
            @RequestBody @Valid ShipmentCreateRequest request
    ) {
        Long shipperId = Long.valueOf(authentication.getPrincipal().toString());
        shipmentService.createShipment(shipperId, request);
    }

    /**
     * 내 화물 목록 조회 (최신순 정렬 및 상태 필터링)
     * GET /api/shipment/my?status=IN_TRANSIT
     */
    @GetMapping("/my")
    public ResponseEntity<List<ShipmentListResponse>> getMyShipments(
            Authentication authentication,
            @RequestParam(name = "status", required = false) ShipmentStatus status
    ) {
        // 인증 객체에서 화주 ID 추출
        Long shipperId = Long.valueOf(authentication.getPrincipal().toString());

        // 서비스 호출 (상태 필터가 null이면 전체 조회, 있으면 필터 조회)
        List<ShipmentListResponse> responses = shipmentService.getMyShipmentList(shipperId, status);

        return ResponseEntity.ok(responses);
    }

    /**
     * 화물 상세 정보 조회 (추가됨)
     * GET /api/shipment/{shipmentId}
     * 화주와 차주 화면에서 실시간 위치 및 ETA를 확인하기 위해 사용합니다.
     */
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentDetailResponse> getShipmentDetail(
            @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        // 서비스에서 Google Directions API 연동 로직이 포함된 상세 데이터를 가져옵니다.
        ShipmentDetailResponse response = shipmentService.getShipmentDetail(shipmentId);

        return ResponseEntity.ok(response);
    }

    /**
     * 화주용 정산 요약 정보 조회
     * 이번 달 지출 총액 및 저번 달 대비 차액 반환
     */
    @GetMapping("/shipper/settlement/summary")
    public ResponseEntity<ShipperSettlementSummaryResponse> getSummary(Authentication authentication) {
        Long shipperId = Long.valueOf(authentication.getPrincipal().toString());

        return ResponseEntity.ok(shipmentService.getShipperSettlementSummary(shipperId));
    }

    @GetMapping("/driver/settlement/summary")
    public ResponseEntity<DriverSettlementSummaryResponse> getDriverSummary(Authentication authentication) {
        // 차주의 Principal 정보에서 ID 추출 (기존 방식 유지)
        Long driverId = Long.valueOf(authentication.getPrincipal().toString());

        return ResponseEntity.ok(shipmentService.getDriverSettlementSummary(driverId));
    }
}