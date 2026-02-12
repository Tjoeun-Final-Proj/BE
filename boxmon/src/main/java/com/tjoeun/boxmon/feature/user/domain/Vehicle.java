package com.tjoeun.boxmon.feature.user.domain;

import com.tjoeun.boxmon.feature.user.domain.Driver;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "VEHICLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    // 차주와의 관계 (이미지상 1:N 또는 1:1 관계선 참고)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "vehicle_number", nullable = false, unique = true)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private VehicleType vehicleType;

    @Column(name = "can_refrigerate", nullable = false)
    @Builder.Default
    private Boolean canRefrigerate = false; // 냉장 가능 여부

    @Column(name = "can_freeze", nullable = false)
    @Builder.Default
    private Boolean canFreeze = false; // 냉동 가능 여부

    @Column(name = "weight_capacity", nullable = false)
    private Double weightCapacity; // 적재 가능 중량
}