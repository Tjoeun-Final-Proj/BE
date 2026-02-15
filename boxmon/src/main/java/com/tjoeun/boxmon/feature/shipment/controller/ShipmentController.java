package com.tjoeun.boxmon.feature.shipment.controller;

import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
     * Get shipment detail for shared shipper/driver detail screen.
     */
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentDetailResponse> getShipmentDetail(
            @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        // Service computes ETA/distance when current driver location exists.
        ShipmentDetailResponse response = shipmentService.getShipmentDetail(shipmentId);
        return ResponseEntity.ok(response);
    }
}