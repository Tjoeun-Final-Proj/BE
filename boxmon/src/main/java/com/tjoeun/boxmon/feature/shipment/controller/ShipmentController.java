package com.tjoeun.boxmon.feature.shipment.controller;

import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentListResponse;
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
}