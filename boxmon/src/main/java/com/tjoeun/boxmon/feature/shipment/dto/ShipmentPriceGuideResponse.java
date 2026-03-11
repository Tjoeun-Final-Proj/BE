package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentPriceGuideResponse {

    private Double estimatedDistanceKm;

    private Integer recommendedPrice;
}
