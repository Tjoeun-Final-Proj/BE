package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.settlement.dto.SettlementViewStatus;
import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 운송사 정산 목록 응답 DTO
 * 화주 기준으로 정산 대상 운송건 목록 조회에서 사용
 */
@Getter
@Builder
public class DriverSettlementListResponse {
    private Long shipmentId;             // 운송 ID
    private String shipmentStatus;        // 운송 상태(한국어)
    private SettlementViewStatus settlementStatus; // 정산 상태
    private LocalDateTime createdAt;      // 운송 생성 일시
    private LocalDateTime pickupDesiredAt; // 픽업 희망 일시
    private LocalDateTime dropoffDesiredAt; // 배차 희망 일시
    private String pickupAddress;         // 픽업 주소
    private String dropoffAddress;        // 하차 주소
    private BigDecimal profit;            // 정산 예상 수익
}
