package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import lombok.*;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 화물 상세 조회 응답 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDetailResponse {
    private Long shipmentId;
    private String shipmentNumber;
    private ShipmentStatus shipmentStatus;
    private LocalDateTime createdAt;

    private Long shipperId;
    private String shipperName;
    private Long driverId;
    private String driverName;

    private Point currentDriverPoint;
    private String distanceToDestination;
    private LocalDateTime estimatedArrivalTime;

    private String pickupAddress;
    private String waypoint1Address;
    private String waypoint2Address;
    private String dropoffAddress;

    private LocalDateTime pickupDesiredAt;
    private LocalDateTime dropoffDesiredAt;

    private CargoType cargoType;
    private String cargoVolume;
    private Double cargoWeight;
    private String vehicleType;
    private String description;

    private BigDecimal price;
    private BigDecimal platformFee;
    private BigDecimal profit;

    private Point pickupPoint;
    private Point dropoffPoint;
    private String cargoPhotoUrl;
    private String dropoffPhotoUrl;
}
