package com.tjoeun.boxmon.feature.user.dto;

import com.tjoeun.boxmon.feature.user.domain.VehicleType; // 1. Enum 임포트 추가
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRegistrationRequest {

    @NotBlank(message = "차량 번호는 필수입니다.")
    private String vehicleNumber;

    @NotNull(message = "차량 종류는 필수입니다.") // 2. String -> VehicleType 변경 및 NotNull 추가
    private VehicleType vehicleType;

    @NotNull(message = "냉장 가능 여부는 필수입니다.")
    private Boolean canRefrigerate;

    @NotNull(message = "냉동 가능 여부는 필수입니다.")
    private Boolean canFreeze;

    @NotNull(message = "적재 가능 중량은 필수입니다.")
    private Double weightCapacity;
}