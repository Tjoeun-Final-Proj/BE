package com.tjoeun.boxmon.global.systemsetting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.domain.AdminEventType;
import com.tjoeun.boxmon.feature.admin.domain.EventLog;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeChangeHistoryResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeSettingResponse;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.admin.repository.EventLogRepository;
import com.tjoeun.boxmon.global.systemsetting.domain.SystemSetting;
import com.tjoeun.boxmon.global.systemsetting.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemSettingServiceImpl implements SystemSettingService {

    private static final String FEE_SETTING_ID = "fee";
    private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.1");

    private final SystemSettingRepository systemSettingRepository;
    private final AdminRepository adminRepository;
    private final EventLogRepository eventLogRepository;

    @PostConstruct
    @Transactional
    public void initializeFeeSettingIfAbsent() {
        // 애플리케이션 초기화 시 fee 설정 row를 보장한다.
        if (systemSettingRepository.findById(FEE_SETTING_ID).isEmpty()) {
            systemSettingRepository.save(new SystemSetting(FEE_SETTING_ID, DEFAULT_FEE_RATE.toPlainString()));
        }
    }

    @Override
    public BigDecimal getFeeRateOrDefault() {
        // 화물 등록 계산에 사용할 수수료율을 조회하고, 누락/이상값이면 기본값으로 안전하게 대체한다.
        Optional<SystemSetting> settingOptional = systemSettingRepository.findById(FEE_SETTING_ID);

        if (settingOptional.isEmpty()) {
            log.warn("system_setting fee row가 없어 기본값 {}을 사용합니다.", DEFAULT_FEE_RATE);
            return DEFAULT_FEE_RATE;
        }

        String rawValue = settingOptional.get().getValue();
        Optional<BigDecimal> parsed = parseFeeRate(rawValue);
        if (parsed.isEmpty()) {
            log.warn("system_setting fee 값이 비정상({})이라 기본값 {}을 사용합니다.", rawValue, DEFAULT_FEE_RATE);
            return DEFAULT_FEE_RATE;
        }

        BigDecimal feeRate = parsed.get();
        if (!isInRange(feeRate)) {
            log.warn("system_setting fee 값 범위 오류({})로 기본값 {}을 사용합니다.", rawValue, DEFAULT_FEE_RATE);
            return DEFAULT_FEE_RATE;
        }

        return feeRate;
    }

    @Override
    public AdminFeeSettingResponse getFeeSetting(Long adminId) {
        // 관리자 권한 검증 후 현재 fee 설정의 원본값과 실제 적용값을 함께 반환한다.
        validateAdminAccess(adminId);

        SystemSetting setting = systemSettingRepository.findById(FEE_SETTING_ID)
                .orElseGet(() -> new SystemSetting(FEE_SETTING_ID, DEFAULT_FEE_RATE.toPlainString()));

        return AdminFeeSettingResponse.builder()
                .settingId(setting.getSettingId())
                .value(setting.getValue())
                .effectiveFeeRate(getFeeRateOrDefault())
                .build();
    }

    @Override
    @Transactional
    public AdminFeeSettingResponse updateFeeSetting(Long adminId, String value) {
        // 관리자 권한과 입력값(숫자/범위)을 검증한 뒤 fee 설정을 저장한다.
        Admin admin = getAdminOrThrow(adminId);

        BigDecimal feeRate = parseFeeRate(value)
                .orElseThrow(() -> new IllegalArgumentException("수수료율 값은 숫자 문자열이어야 합니다."));
        if (!isInRange(feeRate)) {
            throw new IllegalArgumentException("수수료율은 0 이상 1 이하여야 합니다.");
        }

        String normalizedValue = feeRate.stripTrailingZeros().toPlainString();

        SystemSetting setting = systemSettingRepository.findById(FEE_SETTING_ID)
                .orElseGet(() -> new SystemSetting(FEE_SETTING_ID, normalizedValue));
        String beforeValue = setting.getValue();
        setting.updateValue(normalizedValue);
        systemSettingRepository.save(setting);
        eventLogRepository.save(EventLog.builder()
                .admin(admin)
                .eventType(AdminEventType.FEE_RATE_CHANGED)
                .payload(buildFeeChangePayload(setting.getSettingId(), beforeValue, normalizedValue))
                .build());

        return AdminFeeSettingResponse.builder()
                .settingId(setting.getSettingId())
                .value(setting.getValue())
                .effectiveFeeRate(feeRate)
                .build();
    }

    @Override
    public List<AdminFeeChangeHistoryResponse> getFeeSettingHistory(Long adminId) {
        validateAdminAccess(adminId);

        return eventLogRepository.findByEventTypeOrderByCreatedAtDesc(AdminEventType.FEE_RATE_CHANGED)
                .stream()
                .map(this::toFeeHistoryResponse)
                .toList();
    }

    private AdminFeeChangeHistoryResponse toFeeHistoryResponse(EventLog eventLog) {
        JsonNode payload = eventLog.getPayload();
        String beforeValue = payloadText(payload, "beforeValue");
        String afterValue = payloadText(payload, "afterValue");

        return AdminFeeChangeHistoryResponse.builder()
                .logId(eventLog.getLogId())
                .adminId(eventLog.getAdmin().getAdminId())
                .adminName(eventLog.getAdmin().getName())
                .eventType(eventLog.getEventType().name())
                .eventTypeDescription(eventLog.getEventType().getDescription())
                .createdAt(eventLog.getCreatedAt())
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .changedBy(eventLog.getAdmin().getName())
                .payload(payload)
                .build();
    }

    private String payloadText(JsonNode payload, String key) {
        if (payload == null || !payload.hasNonNull(key)) {
            return null;
        }
        return payload.get(key).asText();
    }

    private JsonNode buildFeeChangePayload(String settingId, String beforeValue, String afterValue) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("settingId", settingId);
        payload.put("beforeValue", beforeValue);
        payload.put("afterValue", afterValue);
        payload.put("effectiveFeeRate", afterValue);
        payload.put("changed", true);
        return payload;
    }

    private Optional<BigDecimal> parseFeeRate(String rawValue) {
        // 문자열 설정값을 수수료율 숫자로 파싱한다.
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(rawValue.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private boolean isInRange(BigDecimal value) {
        // 수수료율 허용 범위(0~1) 여부를 판별한다.
        return value.compareTo(BigDecimal.ZERO) >= 0 && value.compareTo(BigDecimal.ONE) <= 0;
    }

    private void validateAdminAccess(Long adminId) {
        // JWT principal이 실제 관리자 계정인지 확인한다.
        if (!adminRepository.existsById(adminId)) {
            throw new RoleAccessDeniedException("Admin access required.");
        }
    }

    private Admin getAdminOrThrow(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new RoleAccessDeniedException("Admin access required."));
    }
}
