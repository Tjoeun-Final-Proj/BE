package com.tjoeun.boxmon.feature.admin.dto;

import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAssignedShipmentBasicResponse {
    private Long shipmentId;
    private String driverName;
    private String pickupAddress;
    private String dropoffAddress;
    private ShipmentStatus shipmentStatus;
}
