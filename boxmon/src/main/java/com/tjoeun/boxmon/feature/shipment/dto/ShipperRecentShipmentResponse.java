package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShipperRecentShipmentResponse {
    private Long shipmentId;
    private String routeText;
    private String shipmentStatus;
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedLabel;
}
