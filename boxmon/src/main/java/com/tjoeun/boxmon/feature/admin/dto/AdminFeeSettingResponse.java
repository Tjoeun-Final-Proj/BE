package com.tjoeun.boxmon.feature.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 관리자 수수료 설정 조회/수정 응답 DTO.
 */
@Getter
@Builder
@AllArgsConstructor
public class AdminFeeSettingResponse {
    // 설정 키 (현재는 "fee")
    private String settingId;
    // DB에 저장된 원본 문자열 값
    private String value;
    // 파싱/검증 후 실제 계산에 적용되는 수수료율
    private BigDecimal effectiveFeeRate;
}
