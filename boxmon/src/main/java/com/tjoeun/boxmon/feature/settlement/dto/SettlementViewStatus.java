package com.tjoeun.boxmon.feature.settlement.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementViewStatus {
    INELIGIBLE("정산 불가"), // 운송 완료 후 결제된 금액이 플랫폼에 정산되기 전의 상태
    READY("정산 미완료"),    // 플랫폼이 정산을 받아 차주에게 정산이 가능한 상태
    PROGRESS("정산 진행중"), // 차주 정산 과정이 진행중인 상태
    PAID("정산 완료");      // 정산 처리가 끝난 상태

    private final String description;
}