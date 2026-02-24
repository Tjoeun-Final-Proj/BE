package com.tjoeun.boxmon.feature.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminFeeSettingUpdateRequest {
    @NotBlank(message = "value는 필수입니다.")
    private String value;
}
