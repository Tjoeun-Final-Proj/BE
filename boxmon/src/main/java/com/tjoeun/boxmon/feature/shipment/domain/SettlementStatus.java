package com.tjoeun.boxmon.feature.shipment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
    PENDING("정산 전"), // 운송 완료 후 결제된 금액이 플랫폼에 정산되기 전의 상태
    PROGRESS("정산 진행중"), // 차주 정산 과정이 진행중인 상태
    PAID("정산 완료"),      // 정산 처리가 끝난 상태
    HOLD("정산 보류"); //정산 실패로 인한 보류 상태

    private final String description;
}