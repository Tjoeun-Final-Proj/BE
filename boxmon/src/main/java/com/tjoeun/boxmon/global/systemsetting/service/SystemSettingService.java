package com.tjoeun.boxmon.global.systemsetting.service;

import com.tjoeun.boxmon.feature.admin.dto.AdminFeeSettingResponse;

import java.math.BigDecimal;

public interface SystemSettingService {
    BigDecimal getFeeRateOrDefault();

    AdminFeeSettingResponse getFeeSetting(Long adminId);

    AdminFeeSettingResponse updateFeeSetting(Long adminId, String value);
}
