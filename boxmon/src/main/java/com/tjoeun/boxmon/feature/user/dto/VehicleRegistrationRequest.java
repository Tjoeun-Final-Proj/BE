package com.tjoeun.boxmon.feature.user.dto;

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

    private String vehicleType; // 차량 종류

    @NotNull(message = "냉장 가능 여부는 필수입니다.")
    private Boolean canRefrigerate;

    @NotNull(message = "냉동 가능 여부는 필수입니다.")
    private Boolean canFreeze;

    @NotNull(message = "적재 가능 중량은 필수입니다.")
    private Double weightCapacity;
}
