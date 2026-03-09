package com.tjoeun.boxmon.feature.settlement.service;

import org.springframework.stereotype.Component;

@Component
public interface SettlementNotifier {
    void onShipmentCompleted(Long shipmentId);
}
