package com.tjoeun.boxmon.feature.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationRouteResponse {
    private Long shipmentId;
    private int pointCount;
    private boolean truncated;
    private List<LocationRoutePointResponse> points;
}
