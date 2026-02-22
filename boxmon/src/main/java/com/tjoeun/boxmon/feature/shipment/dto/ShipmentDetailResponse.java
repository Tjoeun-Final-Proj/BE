package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import lombok.*;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 배송 상세 정보를 응답하는 DTO 클래스입니다.
 * 화주 및 운송 기사가 배송의 상세 현황을 확인할 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDetailResponse {
    // 1. 기본 식별 및 상태
    private Long shipmentId; // 배송 고유 ID
    private String shipmentNumber;    // GEN-260212-001 형식의 생성된 화물 번호
    private ShipmentStatus shipmentStatus; // 배송 상태
    private LocalDateTime createdAt; // 배송 생성 일시

    // 2. 인적 정보
    private Long shipperId; // 화주 ID
    private String shipperName;      // 화주 이름
    private Long driverId; // 운송 기사 ID
    private String driverName;       // 운송 기사 이름
    // private String driverPhotoUrl; // 운송 기사 프로필 사진 URL (주석 처리됨)

    // 3. 실시간 위치 및 Google Maps 정보
    private Point currentDriverPoint;       // 운송 기사(차량)의 최신 경위도 좌표 (지도 마커용)
    private String distanceToDestination;    // 목적지까지 남은 거리 정보 (포맷팅된 문자열)
    private LocalDateTime estimatedArrivalTime; // 예상 도착 시간 (시스템 현재 시간 + 소요 시간)

    // 4. 주소 정보
    private String pickupAddress; // 상차지 주소
    private String waypoint1Address; // 첫 번째 경유지 주소
    private String waypoint2Address; // 두 번째 경유지 주소
    private String dropoffAddress; // 하차지 주소

    // 5. 시간 정보
    private LocalDateTime pickupDesiredAt; // 화주 희망 상차 시간
    private LocalDateTime dropoffDesiredAt; // 화주 희망 하차 시간

    // 6. 화물 및 차량 상세 정보
    private CargoType cargoType;             // 화물 종류 (Enum, 프론트에서 code/description 모두 활용 가능)
    private String cargoVolume;              // 화물 부피 (예: 5미터 가구)
    private Double cargoWeight;              // 화물 중량 (예: 5.0 톤)
    private String vehicleType;
    private String description;              // 화물 특이사항

    // 7. 금액 정보
    private BigDecimal price;                // 총 운임 (화주 지불액)
    private BigDecimal platformFee;          // 플랫폼 수수료
    private BigDecimal profit;               // 운송 기사 수익 (운송료)

    // 8. 정적 좌표
    private Point pickupPoint; // 상차지 경위도 좌표
    private Point dropoffPoint; // 하차지 경위도 좌표
    private String dropoffPhotoUrl;
}
