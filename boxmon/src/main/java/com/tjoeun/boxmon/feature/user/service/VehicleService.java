package com.tjoeun.boxmon.feature.user.service;

import com.tjoeun.boxmon.feature.user.dto.VehicleRegistrationRequest;

public interface VehicleService {
    void registerVehicle(Long driverId, VehicleRegistrationRequest request);
}
