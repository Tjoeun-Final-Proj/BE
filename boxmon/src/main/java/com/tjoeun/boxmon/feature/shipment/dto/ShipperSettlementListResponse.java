package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 화주 정산 목록 응답 DTO
 * 화주 기준으로 정산 내역 목록 조회에서 사용
 */
@Getter
@Builder
public class ShipperSettlementListResponse {
    private Long shipmentId;             // 운송 ID
    private String shipmentStatus;        // 운송 상태(한국어)
    private SettlementStatus settlementStatus; // 정산 상태
    private LocalDateTime createdAt;      // 운송 생성 일시
    private LocalDateTime pickupDesiredAt; // 픽업 희망 일시
    private LocalDateTime dropoffDesiredAt; // 하차 희망 일시
    private String pickupAddress;         // 픽업 주소
    private String dropoffAddress;        // 하차 주소
    private BigDecimal price;             // 화주 부담 결제 금액
}
