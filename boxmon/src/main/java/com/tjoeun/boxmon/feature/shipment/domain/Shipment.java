package com.tjoeun.boxmon.feature.shipment.domain;

import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

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
    private Long shipmentId;

    // 화주 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private Shipper shipper;

    // 차주 식별자 (배차 전에는 NULL 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt; // 배차 수락 시간

    // --- 출발지 및 도착지 정보 ---
    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "pickup_desired_at", nullable = false)
    private LocalDateTime pickupDesiredAt; // 화주 희망 출발 시간

    @Column(name = "pickup_at")
    private LocalDateTime pickupAt; // 실제 출발 시간

    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    @Column(name = "dropoff_desired_at", nullable = false)
    private LocalDateTime dropoffDesiredAt; // 화주 희망 도착 시간

    @Column(name = "dropoff_at")
    private LocalDateTime dropoffAt; // 실제 도착 시간

    // --- 경유지 정보 (최대 2곳) ---
    @Column(name = "waypoint1_address")
    private String waypoint1Address;

    @Column(name = "waypoint1_at")
    private LocalDateTime waypoint1At;

    @Column(name = "waypoint2_address")
    private String waypoint2Address;

    @Column(name = "waypoint2_at")
    private LocalDateTime waypoint2At;

    // --- 비용 및 수익 ---
    @Column(name = "price", nullable = false)
    private Integer price; // 운임 전체 비용

    @Column(name = "platform_fee", nullable = false)
    private Integer platformFee; // 수수료

    @Column(name = "profit", nullable = false)
    private Integer profit; // 차주 수익

    // --- 화물 상세 정보 ---
    @Column(name = "shipment_status", nullable = false)
    private String shipmentStatus; // REQUESTED, ASSIGNED, IN_TRANSIT, DONE, CANCELED

    @Column(name = "cargo_type", nullable = false)
    private String cargoType;

    @Column(name = "cargo_weight", nullable = false)
    private Double cargoWeight;

    @Column(name = "cargo_volume")
    private String cargoVolume;

    @Column(name = "need_refrigerate", nullable = false)
    @Builder.Default
    private Boolean needRefrigerate = false;

    @Column(name = "need_freeze", nullable = false)
    @Builder.Default
    private Boolean needFreeze = false;

    @Column(columnDefinition = "TEXT")
    private String description; // 특이사항

    @Column(name = "cargo_photo_url")
    private String cargoPhotoUrl; // AWS S3 화물 등록 사진

    @Column(name = "dropoff_photo_url")
    private String dropoffPhotoUrl; // AWS S3 하차 완료 사진

    // --- 취소 관련 토글 ---
    @Column(name = "shipper_cancel_toggle", nullable = false)
    @Builder.Default
    private Boolean shipperCancelToggle = false;

    @Column(name = "driver_cancel_toggle", nullable = false)
    @Builder.Default
    private Boolean driverCancelToggle = false;

    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point pickupPoint; // 출발 경위도

    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point dropoffPoint; // 도착 경위도

    @Column(columnDefinition = "POINT SRID 4326")
    private Point waypoint1Point;

    @Column(columnDefinition = "POINT SRID 4326")
    private Point waypoint2Point;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 화물 등록 시간

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

    