package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;

public interface ShipmentService {
    void createShipment(Long shipperId, ShipmentCreateRequest request);
}
