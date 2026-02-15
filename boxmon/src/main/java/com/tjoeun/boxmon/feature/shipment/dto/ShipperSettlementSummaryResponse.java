package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 화주 정산 요약 응답 DTO 입니다.
 * 화주의 월별 총 정산 금액 및 전월 대비 차이 정보를 제공합니다.
 */
@Getter
@Builder
public class ShipperSettlementSummaryResponse {
    private BigDecimal thisMonthTotalAmount; // 이번 달 총 정산 금액
    private BigDecimal lastMonthTotalAmount; // 저번 달 총 정산 금액
    private BigDecimal difference;           // 두 달의 차액 (this - last)
}