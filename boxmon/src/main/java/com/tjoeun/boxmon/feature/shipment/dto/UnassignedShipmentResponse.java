package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.user.domain.VehicleType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class UnassignedShipmentResponse {
    private Long shipmentId;
    private String pickupAddress;
    private String dropoffAddress;
    private LocalDateTime pickupDesiredAt;
    private LocalDateTime dropoffDesiredAt;
    private Double estimatedDistance;
    private Double cargoWeight;
    private String vehicleType;
    private String description;
    private BigDecimal profit;
}
