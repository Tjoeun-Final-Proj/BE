package com.tjoeun.boxmon.feature.settlement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettlementRequest {
    @NotNull
    private Long shipmentId;
}
