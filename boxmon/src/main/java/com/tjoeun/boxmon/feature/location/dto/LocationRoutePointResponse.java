package com.tjoeun.boxmon.feature.location.dto;

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
public class LocationRoutePointResponse {
    private Double lat;
    private Double lng;
    private String at;
}
