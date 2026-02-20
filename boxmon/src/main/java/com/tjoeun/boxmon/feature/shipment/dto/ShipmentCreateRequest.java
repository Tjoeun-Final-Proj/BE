package com.tjoeun.boxmon.feature.shipment.dto;

import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import com.tjoeun.boxmon.feature.user.domain.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

/**
 * 새로운 배송을 생성하기 위한 요청 DTO 입니다.
 * 화주가 배송 정보를 입력할 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentCreateRequest {

    @NotNull(message = "출발지 좌표는 필수입니다.")
    private Point pickupPoint; // 출발지 경위도 좌표

    @NotBlank(message = "출발지 주소는 필수입니다.")
    private String pickupAddress; // 출발지 주소

    @NotNull(message = "화주 희망 출발 시간은 필수입니다.")
    private LocalDateTime pickupDesiredAt; // 화주 희망 출발 시간

    @NotNull(message = "도착지 좌표는 필수입니다.")
    private Point dropoffPoint; // 도착지 경위도 좌표

    @NotBlank(message = "도착지 주소는 필수입니다.")
    private String dropoffAddress; // 도착지 주소

    @NotNull(message = "화주 희망 도착 시간은 필수입니다.")
    private LocalDateTime dropoffDesiredAt; // 화주 희망 도착 시간

    private Point waypoint1Point; // 첫 번째 경유지 경위도 좌표
    private String waypoint1Address; // 첫 번째 경유지 주소
    private Point waypoint2Point; // 두 번째 경유지 경위도 좌표
    private String waypoint2Address; // 두 번째 경유지 주소

    private Double estimatedDistance; // 운송 예상 거리 (단위: km)

    @NotNull(message = "운임은 필수입니다.")
    private Integer price; // 운임

    @NotNull(message = "화물 종류는 필수입니다.")
    private CargoType cargoType; // 화물 종류

    @NotNull(message = "화물 중량은 필수입니다.")
    private Double cargoWeight; // 화물 중량

    private String cargoVolume; // 화물 부피

    @NotNull(message = "희망 차량 종류는 필수입니다.")
    private VehicleType vehicleType; // 희망 차량 종류

    @NotNull(message = "냉장 필요 여부는 필수입니다.")
    private Boolean needRefrigerate; // 냉장 필요 여부

    @NotNull(message = "냉동 필요 여부는 필수입니다.")
    private Boolean needFreeze; // 냉동 필요 여부

    private String description; // 특이사항

    private String cargoPhotoUrl; // 화물 사진 URL
}
