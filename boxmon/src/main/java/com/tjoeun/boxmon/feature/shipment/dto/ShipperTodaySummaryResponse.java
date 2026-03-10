package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShipperTodaySummaryResponse {
    private int requestedCount;
    private int assignedCount;
    private int inTransitCount;
    private int doneCount;
}
