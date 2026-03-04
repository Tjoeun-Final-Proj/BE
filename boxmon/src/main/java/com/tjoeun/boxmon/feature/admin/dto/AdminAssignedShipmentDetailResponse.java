package com.tjoeun.boxmon.feature.admin.dto;

import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.user.domain.VehicleType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminAssignedShipmentDetailResponse {
    private Long shipmentId;
    private Long shipperId;
    private Long driverId;
    private LocalDateTime acceptedAt;
    private String pickupAddress;
    private LocalDateTime pickupDesiredAt;
    private LocalDateTime pickupAt;
    private String dropoffAddress;
    private LocalDateTime dropoffDesiredAt;
    private LocalDateTime dropoffAt;
    private String waypoint1Address;
    private LocalDateTime waypoint1At;
    private String waypoint2Address;
    private LocalDateTime waypoint2At;
    private Double estimatedDistance;
    private BigDecimal price;
    private BigDecimal platformFee;
    private BigDecimal profit;
    private ShipmentStatus shipmentStatus;
    private CargoType cargoType;
    private Double cargoWeight;
    private String cargoVolume;
    private VehicleType vehicleType;
    private Boolean needRefrigerate;
    private Boolean needFreeze;
    private String description;
    private String cargoPhotoUrl;
    private String dropoffPhotoUrl;
    private Boolean shipperCancelToggle;
    private Boolean driverCancelToggle;
    private Point pickupPoint;
    private Point dropoffPoint;
    private Point waypoint1Point;
    private Point waypoint2Point;
    private LocalDateTime createdAt;
    private Point currentLocationPoint;
}
