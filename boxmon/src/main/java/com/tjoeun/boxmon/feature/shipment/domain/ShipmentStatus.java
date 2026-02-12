package com.tjoeun.boxmon.feature.shipment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipmentStatus {
    REQUESTED("배차 대기"),
    ASSIGNED("배차 완료"),
    IN_TRANSIT("운송 중"),
    DONE("운송 완료"),
    CANCELED("취소됨");

    private final String description;
}