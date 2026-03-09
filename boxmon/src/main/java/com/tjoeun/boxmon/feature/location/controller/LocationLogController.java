package com.tjoeun.boxmon.feature.location.controller;

import com.tjoeun.boxmon.feature.location.dto.LocationLogRequest;
import com.tjoeun.boxmon.feature.location.dto.LocationRouteResponse;
import com.tjoeun.boxmon.feature.location.service.LocationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    @GetMapping("/{shipmentId}/route")
    public ResponseEntity<LocationRouteResponse> getRoute(
            Authentication authentication,
            @PathVariable Long shipmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer maxPoints
    ) {
        Long requesterId = Long.valueOf(authentication.getPrincipal().toString());
        LocationRouteResponse response = locationLogService.getRoute(requesterId, shipmentId, from, to, maxPoints);
        return ResponseEntity.ok(response);
    }
}
