package com.tjoeun.boxmon.feature.shipment.controller;

import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.DriverSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipments/settlements")
@RequiredArgsConstructor
public class ShipmentSettlementController {

    private final ShipmentService shipmentService;

    @GetMapping("/shipper")
    public ResponseEntity<List<ShipperSettlementListResponse>> getShipperSettlementList(
            Authentication authentication,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month,
            @RequestParam(name = "shipmentStatus", required = false) ShipmentStatus shipmentStatus,
            @RequestParam(name = "settlementStatus", required = false) SettlementStatus settlementStatus
    ) {
        Long shipperId = Long.valueOf(authentication.getPrincipal().toString());
        List<ShipperSettlementListResponse> responses =
                shipmentService.getShipperSettlementList(shipperId, year, month, shipmentStatus, settlementStatus);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/driver")
    public ResponseEntity<List<DriverSettlementListResponse>> getDriverSettlementList(
            Authentication authentication,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month,
            @RequestParam(name = "shipmentStatus", required = false) ShipmentStatus shipmentStatus,
            @RequestParam(name = "settlementStatus", required = false) SettlementStatus settlementStatus
    ) {
        Long driverId = Long.valueOf(authentication.getPrincipal().toString());
        List<DriverSettlementListResponse> responses =
                shipmentService.getDriverSettlementList(driverId, year, month, shipmentStatus, settlementStatus);
        return ResponseEntity.ok(responses);
    }
}
