package com.tjoeun.boxmon.feature.shipment.domain;

import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.domain.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 배송(Shipment) 정보를 담는 엔티티 클래스입니다.
 * 화물 운송과 관련된 모든 상세 정보를 관리합니다.
 */
@Entity
@Table(name = "SHIPMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long shipmentId; // 배송 고유 식별자

    // 화주 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private Shipper shipper; // 배송을 요청한 화주 정보

    // 차주 식별자 (배차 전에는 NULL 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver; // 배송을 수락한 운송 기사 정보 (배차 전에는 NULL 가능)

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt; // 운송 기사가 배차를 수락한 시간

    // --- 출발지 및 도착지 정보 ---
    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress; // 출발지 주소

    @Column(name = "pickup_desired_at", nullable = false)
    private LocalDateTime pickupDesiredAt; // 화주가 희망하는 출발 시간

    @Column(name = "pickup_at")
    private LocalDateTime pickupAt; // 실제 출발 시간

    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress; // 도착지 주소

    @Column(name = "dropoff_desired_at", nullable = false)
    private LocalDateTime dropoffDesiredAt; // 화주가 희망하는 도착 시간

    @Column(name = "dropoff_at")
    private LocalDateTime dropoffAt; // 실제 도착 시간

    // --- 경유지 정보 (최대 2곳) ---
    @Column(name = "waypoint1_address")
    private String waypoint1Address; // 첫 번째 경유지 주소

    @Column(name = "waypoint1_at")
    private LocalDateTime waypoint1At; // 첫 번째 경유지 도착 시간

    @Column(name = "waypoint2_address")
    private String waypoint2Address; // 두 번째 경유지 주소

    @Column(name = "waypoint2_at")
    private LocalDateTime waypoint2At; // 두 번째 경유지 도착 시간

    @Column(name = "estimated_distance")
    private Double estimatedDistance; // 운송 예상 거리 (단위: km)

    // --- 비용 및 수익 ---
    @Column(name = "price", nullable = false)
    private BigDecimal price; // 총 운임 비용

    @Column(name = "platform_fee", nullable = false)
    private BigDecimal platformFee; // 플랫폼 수수료

    @Column(name = "profit", nullable = false)
    private BigDecimal profit; // 운송 기사 수익

    // --- 화물 상세 정보 ---
    @Enumerated(EnumType.STRING) // DB에 문자열(REQUESTED 등)로 저장
    @Column(name = "shipment_status", nullable = false)
    private ShipmentStatus shipmentStatus; // 배송 현재 상태 (예: 요청됨, 수락됨, 운송 중 등)

    @Column(name = "cargo_type", nullable = false)
    @Enumerated(EnumType.STRING) // Enum의 이름을 DB에 저장
    private CargoType cargoType; // 화물 종류

    @Column(name = "cargo_weight", nullable = false)
    private Double cargoWeight; // 화물 무게 (단위: kg)

    @Column(name = "cargo_volume")
    private String cargoVolume; // 화물 부피 (예: 1CBM)

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType; // 화주 희망 차량 종류

    @Column(name = "need_refrigerate", nullable = false)
    @Builder.Default
    private Boolean needRefrigerate = false; // 냉장 필요 여부

    @Column(name = "need_freeze", nullable = false)
    @Builder.Default
    private Boolean needFreeze = false; // 냉동 필요 여부

    @Column(columnDefinition = "TEXT")
    private String description; // 화물 특이사항 또는 추가 설명

    @Column(name = "cargo_photo_url")
    private String cargoPhotoUrl; // 화물 등록 시 첨부된 사진 URL (AWS S3)

    @Column(name = "dropoff_photo_url")
    private String dropoffPhotoUrl; // 하차 완료 후 첨부된 사진 URL (AWS S3)

    // --- 취소 관련 토글 ---
    @Column(name = "shipper_cancel_toggle", nullable = false)
    @Builder.Default
    private Boolean shipperCancelToggle = false; // 화주 취소 요청 여부

    @Column(name = "driver_cancel_toggle", nullable = false)
    @Builder.Default
    private Boolean driverCancelToggle = false; // 운송 기사 취소 요청 여부

    @Column(name = "pickup_point", columnDefinition = "POINT SRID 4326", nullable = false)
    private Point pickupPoint; // 출발지 경위도 좌표

    @Column(name = "dropoff_point", columnDefinition = "POINT SRID 4326", nullable = false)
    private Point dropoffPoint; // 도착지 경위도 좌표

    @Column(name = "waypoint1_point", columnDefinition = "POINT SRID 4326")
    private Point waypoint1Point; // 첫 번째 경유지 경위도 좌표

    @Column(name = "waypoint2_point", columnDefinition = "POINT SRID 4326")
    private Point waypoint2Point; // 두 번째 경유지 경위도 좌표

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 화물 정보가 생성된 시간

    @Column(name = "current_location_point", columnDefinition = "POINT SRID 4326")
    private Point currentLocationPoint; // 운송 중인 차량의 최신 경위도 좌표

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    @Builder.Default // 빌더를 사용하지 않고 생성할 때를 대비
    private SettlementStatus settlementStatus = SettlementStatus.INELIGIBLE; // 배송 정산 상태

    /**
     * 엔티티가 영속화되기 전에 호출되어 `createdAt` 필드를 현재 시간으로 자동 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

    