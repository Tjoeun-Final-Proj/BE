package com.tjoeun.boxmon.feature.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUnassignedShipmentBasicResponse {
    private Long shipmentId;
    private String shipperName;
    private String pickupAddress;
    private String dropoffAddress;
}
