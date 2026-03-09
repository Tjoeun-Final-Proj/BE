package com.tjoeun.boxmon.feature.settlement.controller;

import com.tjoeun.boxmon.feature.settlement.dto.SettlementRequest;
import com.tjoeun.boxmon.feature.settlement.service.DriverSettlementUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
public class SettlementController {
    private final DriverSettlementUseCase driverSettlementUseCase;

    @PostMapping("")
    public ResponseEntity<Void> requestSettlement(@Valid @RequestBody SettlementRequest request){
        driverSettlementUseCase.requestSettlement(request);
        return ResponseEntity.ok().build();
    }
}
