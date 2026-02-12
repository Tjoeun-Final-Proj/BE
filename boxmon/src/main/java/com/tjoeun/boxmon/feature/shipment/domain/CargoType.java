package com.tjoeun.boxmon.feature.shipment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CargoType {
    GENERAL("GEN", "일반 화물"),
    PALLET("PAL", "팔레트 화물"),
    LONG_CARGO("LNG", "초장축/장척 화물"),
    HEAVY("HVY", "중량 화물"),
    MOVING("MOV", "이사 화물"),
    BULK("BLK", "벌크 화물"),
    HAZARDOUS("HAZ", "위험물");

    private final String code;  // 화물번호 생성용 (예: GEN-260211-001)
    private final String description; // UI 표시용
}
