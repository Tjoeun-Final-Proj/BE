package com.tjoeun.boxmon.feature.shipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentCreateRequest {

    @NotNull(message = "출발지 좌표는 필수입니다.")
    private Point pickupPoint;

    @NotBlank(message = "출발지 주소는 필수입니다.")
    private String pickupAddress;

    @NotNull(message = "화주 희망 출발 시간은 필수입니다.")
    private LocalDateTime pickupDesiredAt;

    @NotNull(message = "도착지 좌표는 필수입니다.")
    private Point dropoffPoint;

    @NotBlank(message = "도착지 주소는 필수입니다.")
    private String dropoffAddress;

    @NotNull(message = "화주 희망 도착 시간은 필수입니다.")
    private LocalDateTime dropoffDesiredAt;

    private Point waypoint1Point;
    private String waypoint1Address;
    private Point waypoint2Point;
    private String waypoint2Address;

    @NotNull(message = "운임은 필수입니다.")
    private Integer price;

    @NotBlank(message = "화물 종류는 필수입니다.")
    private String cargoType;

    @NotNull(message = "화물 중량은 필수입니다.")
    private Double cargoWeight;

    private String cargoVolume;

    @NotNull(message = "냉장 필요 여부는 필수입니다.")
    private Boolean needRefrigerate;

    @NotNull(message = "냉동 필요 여부는 필수입니다.")
    private Boolean needFreeze;

    private String description;

    private String cargoPhotoUrl;
}
