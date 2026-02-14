package com.tjoeun.boxmon.feature.shipment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
    INELIGIBLE("정산 불가"), // 배차 전, 운송 중 등 정산 단계가 아닐 때
    READY("정산 미완료"),    // 운송 완료 후 정산이 아직 안 된 상태
    PAID("정산 완료");      // 정산 처리가 끝난 상태

    private final String description;
}