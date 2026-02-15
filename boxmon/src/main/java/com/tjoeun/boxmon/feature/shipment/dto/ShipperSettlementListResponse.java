package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ShipperSettlementListResponse {
    private Long shipmentId;
    private ShipmentStatus shipmentStatus;
    private SettlementStatus settlementStatus;
    private LocalDateTime createdAt;
    private LocalDateTime pickupDesiredAt;
    private LocalDateTime dropoffDesiredAt;
    private String pickupAddress;
    private String dropoffAddress;
    private BigDecimal price;
}
