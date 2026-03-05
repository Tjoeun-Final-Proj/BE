package com.tjoeun.boxmon.feature.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminForceCancelRequest {

    @NotBlank(message = "강제취소 사유는 필수입니다.")
    @Size(min = 1, max = 200, message = "강제취소 사유는 1자 이상 200자 이하여야 합니다.")
    private String reason;
}
