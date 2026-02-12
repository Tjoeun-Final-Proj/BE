package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentListResponse {
    private Long shipmentId;

    // 현재 배송 상태 (REQUESTED, ASSIGNED, IN_TRANSIT 등)
    private ShipmentStatus shipmentStatus;

    // 화물 기본 정보
    private CargoType cargoType;
    private Double cargoWeight;

    // 경로 요약
    private String pickupAddress;
    private String dropoffAddress;

    // 시간 정보 (목록에서는 보통 화주가 설정한 희망 시간을 보여줍니다)
    private LocalDateTime pickupDesiredAt;
    private LocalDateTime dropoffDesiredAt;

    // 비용 정보
    private BigDecimal price;

    // 등록 시각
    private LocalDateTime createdAt;

    // 차주 정보 (배차 전엔 "미배차" 등으로 표시하기 위함)
    private String driverName;
    private String driverPhone;
}