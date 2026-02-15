package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 운송 기사용 정산 목록 응답 DTO 입니다.
 * 운송 기사에게 제공되는 각 배송 건에 대한 정산 정보를 요약합니다.
 */
@Getter
@Builder
public class DriverSettlementListResponse {
    private Long shipmentId; // 배송 ID
    private ShipmentStatus shipmentStatus; // 배송 상태
    private SettlementStatus settlementStatus; // 정산 상태
    private LocalDateTime createdAt; // 배송 생성 일시
    private LocalDateTime pickupDesiredAt; // 상차 희망 일시
    private LocalDateTime dropoffDesiredAt; // 하차 희망 일시
    private String pickupAddress; // 상차지 주소
    private String dropoffAddress; // 하차지 주소
    private BigDecimal profit; // 운송으로 얻은 수익
}
