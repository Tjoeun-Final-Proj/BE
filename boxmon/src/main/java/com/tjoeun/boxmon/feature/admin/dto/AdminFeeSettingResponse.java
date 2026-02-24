package com.tjoeun.boxmon.feature.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class AdminFeeSettingResponse {
    private String settingId;
    private String value;
    private BigDecimal effectiveFeeRate;
}
