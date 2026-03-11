package com.tjoeun.boxmon.feature.shipment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.geo.Point;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentPriceGuideRequest {

    @NotNull(message = "출발지 좌표는 필수입니다.")
    private Point pickupPoint;

    @NotNull(message = "도착지 좌표는 필수입니다.")
    private Point dropoffPoint;

    private Point waypoint1Point;

    private Point waypoint2Point;
}
