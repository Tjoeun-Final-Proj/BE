package com.tjoeun.boxmon.feature.location.controller;

import com.tjoeun.boxmon.feature.location.dto.LocationLogRequest;
import com.tjoeun.boxmon.feature.location.service.LocationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location-log")
@RequiredArgsConstructor
public class LocationLogController {

    private final LocationLogService locationLogService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveLocationLog(
            Authentication authentication,
            @RequestBody @Valid LocationLogRequest request
    ) {
        Long driverId = Long.valueOf(authentication.getPrincipal().toString());
        locationLogService.saveLocationLog(driverId, request);
    }
}
