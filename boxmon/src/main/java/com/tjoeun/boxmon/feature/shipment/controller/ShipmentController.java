package com.tjoeun.boxmon.feature.shipment.controller;

import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}