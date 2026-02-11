package com.tjoeun.boxmon.feature.location.domain;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LOCATION_LOG")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    // 운송건 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    // 화주 식별자 (조회 최적화를 위한 비정규화)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private Shipper shipper;

    // 차주 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    /**
     * 10분 단위 위치 데이터 청크
     * 프론트엔드에서 "[{\"lat\":37.5, \"lng\":127.0, \"at\":\"...\"}, ...]" 형태의
     * JSON 문자열을 보내주면 그대로 저장합니다.
     */
    @Column(name = "location_data", nullable = false, columnDefinition = "TEXT")
    private String locationData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 청크가 서버에 저장된 시점

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}