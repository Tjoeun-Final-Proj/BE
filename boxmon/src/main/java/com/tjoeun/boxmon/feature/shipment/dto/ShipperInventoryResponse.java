package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ShipperInventoryResponse {
    private Long shipmentId;
    private String pickupAddress;
    private String waypoint1Address;
    private LocalDateTime waypoint1At;
    private String waypoint2Address;
    private LocalDateTime waypoint2At;
    private String dropoffAddress;
    private LocalDateTime pickupDesiredAt;
    private LocalDateTime dropoffDesiredAt;
    private Double estimatedDistance;
    private Double cargoWeight;
    private String vehicleType;
    private String description;
    private String shipmentStatus;
    private BigDecimal price;
}
