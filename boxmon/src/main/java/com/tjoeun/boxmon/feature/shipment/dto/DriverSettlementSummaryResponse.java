package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DriverSettlementSummaryResponse {
    private BigDecimal thisMonthTotalProfit; // 이번 달 총 수익 (profit 합계)
    private BigDecimal lastMonthTotalProfit; // 저번 달 총 수익
    private BigDecimal difference;            // 전월 대비 차이
}