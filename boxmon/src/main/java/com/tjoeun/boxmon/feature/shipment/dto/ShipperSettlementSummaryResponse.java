package com.tjoeun.boxmon.feature.shipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ShipperSettlementSummaryResponse {
    private BigDecimal thisMonthTotalAmount; // 이번 달 총 정산 금액
    private BigDecimal lastMonthTotalAmount; // 저번 달 총 정산 금액
    private BigDecimal difference;           // 두 달의 차액 (this - last)
}