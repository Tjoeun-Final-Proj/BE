package com.tjoeun.boxmon.feature.admin.controller;

import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentDetailResponse;
import com.tjoeun.boxmon.feature.admin.service.AdminShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/unassigned")
@RequiredArgsConstructor
public class AdminShipmentQueryController {

    private final AdminShipmentService adminShipmentService;

    @GetMapping("/basic")
    public ResponseEntity<List<AdminUnassignedShipmentBasicResponse>> getUnassignedBasic(
            Authentication authentication
    ) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(adminShipmentService.getUnassignedBasic(adminId));
    }

    @GetMapping("/detail/{shipmentId}")
    public ResponseEntity<AdminUnassignedShipmentDetailResponse> getUnassignedDetail(
            Authentication authentication,
            @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(adminShipmentService.getUnassignedDetail(adminId, shipmentId));
    }
}
