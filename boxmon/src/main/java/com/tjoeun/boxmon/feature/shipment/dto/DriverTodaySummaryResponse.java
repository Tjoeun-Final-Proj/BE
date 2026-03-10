package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DriverTodaySummaryResponse {
    private int todayScheduleCount;
    private LocalDateTime firstPickupDesiredAt;
    private int inTransitCount;
}
