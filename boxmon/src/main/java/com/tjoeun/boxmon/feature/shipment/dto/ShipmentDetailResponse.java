package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 화물 상세 조회 응답 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDetailResponse {
    // 1) 기본 정보
    private Long shipmentId; // 배송 고유 ID
    private String shipmentNumber; // 화물 번호
    private ShipmentStatus shipmentStatus; // 배송 상태
    private LocalDateTime createdAt; // 배송 생성일시

    // 2) 사용자 정보
    private Long shipperId; // 화주 ID
    private String shipperName; // 화주 이름
    private Long driverId; // 기사 ID
    private String driverName; // 기사 이름

    // 3) 실시간 위치/거리 정보
    private Point currentDriverPoint; // 기사 현재 좌표
    private String distanceToDestination; // 목적지 거리 문자열
    private LocalDateTime estimatedArrivalTime; // 예상 도착 시간

    // 4) 주소
    private String pickupAddress; // 상차지 주소
    private String waypoint1Address; // 경유지1 주소
    private String waypoint2Address; // 경유지2 주소
    private String dropoffAddress; // 하차지 주소

    // 5) 요청 시간
    private LocalDateTime pickupDesiredAt; // 희망 상차 시간
    private LocalDateTime dropoffDesiredAt; // 희망 하차 시간

    // 6) 화물/차량 정보
    private CargoType cargoType; // 화물 종류
    private String cargoVolume; // 화물 부피
    private Double cargoWeight; // 화물 무게
    private String vehicleType; // 차량 타입
    private String description; // 특이사항

    // 7) 금액 정보
    private BigDecimal price; // 총 운임
    private BigDecimal platformFee; // 플랫폼 수수료
    private BigDecimal profit; // 기사 수익

    // 8) 좌표/사진
    private Point pickupPoint; // 상차지 좌표
    private Point dropoffPoint; // 하차지 좌표
    private String cargoPhotoUrl; // 화물 등록 사진 URL
    private String dropoffPhotoUrl; // 하차 완료 사진 URL
}
