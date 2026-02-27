package com.tjoeun.boxmon.global.systemsetting.service;

import com.tjoeun.boxmon.feature.admin.dto.AdminFeeChangeHistoryResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeSettingResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SystemSettingService {
    BigDecimal getFeeRateOrDefault();

    AdminFeeSettingResponse getFeeSetting(Long adminId);

    AdminFeeSettingResponse updateFeeSetting(Long adminId, String value);

    List<AdminFeeChangeHistoryResponse> getFeeSettingHistory(Long adminId);
}
