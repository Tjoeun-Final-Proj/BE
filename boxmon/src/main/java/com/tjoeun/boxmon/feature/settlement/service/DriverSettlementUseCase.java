package com.tjoeun.boxmon.feature.settlement.service;

import com.tjoeun.boxmon.feature.settlement.dto.SettlementRequest;

public interface DriverSettlementUseCase {
    void requestSettlement(SettlementRequest request);
}
