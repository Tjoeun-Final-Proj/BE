package com.tjoeun.boxmon.feature.admin.dto;

import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUnassignedShipmentDetailResponse {
    private Long shipmentId;
    private String shipperName;
    private String pickupAddress;
    private String dropoffAddress;
    private LocalDateTime pickupDesiredAt;
    private LocalDateTime dropoffDesiredAt;
    private LocalDateTime waypoint1At;
    private LocalDateTime waypoint2At;
    private BigDecimal price;
    private BigDecimal profit;
    private BigDecimal platformFee;
    private CargoType cargoType;
    private Double cargoWeight;
    private String cargoVolume;
    private Boolean needRefrigerate;
    private Boolean needFreeze;
    private String description;
    private String cargoPhotoUrl;
    private LocalDateTime createdAt;
    private String vehicleType;
    private Double estimatedDistance;
}
