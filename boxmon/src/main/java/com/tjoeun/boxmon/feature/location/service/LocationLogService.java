package com.tjoeun.boxmon.feature.location.service;

import com.tjoeun.boxmon.feature.location.dto.LocationLogRequest;
import com.tjoeun.boxmon.feature.location.dto.LocationRouteResponse;

import java.time.LocalDateTime;

public interface LocationLogService {
    void saveLocationLog(Long driverId, LocationLogRequest request);

    LocationRouteResponse getRoute(Long requesterId, Long shipmentId, LocalDateTime from, LocalDateTime to, Integer maxPoints);
}
