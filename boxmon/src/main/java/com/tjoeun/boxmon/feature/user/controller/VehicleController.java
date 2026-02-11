package com.tjoeun.boxmon.feature.user.controller;

import com.tjoeun.boxmon.feature.user.dto.VehicleRegistrationRequest;
import com.tjoeun.boxmon.feature.user.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registerVehicle(
            Authentication authentication,
            @RequestBody @Valid VehicleRegistrationRequest request
    ) {
        Long driverId = Long.valueOf(authentication.getPrincipal().toString());
        vehicleService.registerVehicle(driverId, request);
    }
}
