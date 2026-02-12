package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import lombok.*;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDetailResponse {
    // 1. 기본 식별 및 상태
    private Long shipmentId;
    private String shipmentNumber;    // 추가: GEN-260212-001 형식의 생성된 화물 번호
    private ShipmentStatus shipmentStatus;
    private LocalDateTime createdAt;

    // 2. 인적 정보
    private Long shipperId;
    private String shipperName;      // 화주 이름 (차주가 볼 때)
    private Long driverId;
    private String driverName;       // 차주 이름 (화주가 볼 때)
    private String driverPhotoUrl;

    // 3. 실시간 위치 및 Google Maps 정보
    private Point currentDriverPoint;       // 차주 최신 좌표 (마커용)
    private String distanceToDestination;    // 목적지까지 남은 거리
    private LocalDateTime estimatedArrivalTime; // 예상 도착 시간 (시스템현재시간 + 소요시간)

    // 4. 주소 정보
    private String pickupAddress;
    private String waypoint1Address;
    private String waypoint2Address;
    private String dropoffAddress;

    // 5. 화물 및 차량 상세 정보
    private CargoType cargoType;             // Enum 자체를 넘겨 프론트에서 code/description 모두 활용 가능
    private String cargoVolume;              // 시안의 '화물정보' (예: 5미터 가구)
    private Double cargoWeight;              // 톤수 (예: 5.0)
    private String vehicleType;              // 차종 (예: 윙바디)
    private String description;              // 특이사항

    // 6. 금액 정보 (이미지 시안 반영)
    private BigDecimal price;                // 합계금액 (화주 지불액)
    private BigDecimal platformFee;          // 수수료 (fee)
    private BigDecimal profit;               // 운송료 (차주 수익)

    // 7. 정적 좌표
    private Point pickupPoint;
    private Point dropoffPoint;
}