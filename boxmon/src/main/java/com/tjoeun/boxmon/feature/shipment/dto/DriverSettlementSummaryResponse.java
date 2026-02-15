package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 운송 기사 정산 요약 응답 DTO 입니다.
 * 운송 기사의 월별 총 수익 및 전월 대비 차이 정보를 제공합니다.
 */
@Getter
@Builder
public class DriverSettlementSummaryResponse {
    private BigDecimal thisMonthTotalProfit; // 이번 달 총 수익 (profit 합계)
    private BigDecimal lastMonthTotalProfit; // 저번 달 총 수익
    private BigDecimal difference;            // 전월 대비 차이
}