package com.tjoeun.boxmon.feature.location.service;

import com.tjoeun.boxmon.feature.location.dto.LocationLogRequest;

public interface LocationLogService {
    void saveLocationLog(Long driverId, LocationLogRequest request);
}
