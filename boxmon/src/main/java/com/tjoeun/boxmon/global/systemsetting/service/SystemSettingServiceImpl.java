package com.tjoeun.boxmon.global.systemsetting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.domain.AdminEventType;
import com.tjoeun.boxmon.feature.admin.domain.EventLog;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeChangeHistoryResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeGraphPointResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeGraphResponse;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 시스템 설정(수수료율 등)을 관리하는 서비스 구현체입니다.
 * 수수료율의 조회, 수정, 변경 이력 관리 및 통계 그래프 데이터 생성을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemSettingServiceImpl implements SystemSettingService {

    // 시스템 설정 테이블에서 수수료율을 식별하기 위한 ID
    private static final String FEE_SETTING_ID = "fee";
    // 설정값이 없을 경우 사용할 기본 수수료율 (10%)
    private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.1");
    // 그래프 조회 기간 (최근 14일)
    private static final int GRAPH_DAYS = 14;
    // 한국 시간대 설정
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    // 날짜 포맷터 (yyyy-MM-dd)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SystemSettingRepository systemSettingRepository;
    private final AdminRepository adminRepository;
    private final EventLogRepository eventLogRepository;

    /**
     * 애플리케이션 시작 시 수수료 설정 존재 여부를 확인하고, 없으면 기본값으로 초기화합니다.
     */
    @PostConstruct
    @Transactional
    public void initializeFeeSettingIfAbsent() {
        if (systemSettingRepository.findById(FEE_SETTING_ID).isEmpty()) {
            log.info("초기 수수료 설정을 생성합니다. 기본값: {}", DEFAULT_FEE_RATE);
            systemSettingRepository.save(new SystemSetting(FEE_SETTING_ID, DEFAULT_FEE_RATE.toPlainString()));

            // 초기값 설정 사실을 EventLog에 기록하여 그래프 조회 시 기준점(Seed)으로 사용하게 함
            try {
                // 시스템 초기화 주체를 기록하기 위해 첫 번째 관리자를 찾음 (임시 방편)
                Optional<Admin> firstAdmin = adminRepository.findAll().stream().findFirst();
                if (firstAdmin.isPresent()) {
                    eventLogRepository.save(EventLog.builder()
                            .admin(firstAdmin.get())
                            .eventType(AdminEventType.FEE_RATE_CHANGED)
                            .payload(buildFeeChangePayload(FEE_SETTING_ID, "0", DEFAULT_FEE_RATE.toPlainString()))
                            .build());
                    log.info("초기 수수료 설정 이력(EventLog)을 생성했습니다.");
                } else {
                    log.warn("등록된 관리자가 없어 초기 수수료 설정 이력을 남기지 못했습니다.");
                }
            } catch (Exception e) {
                log.error("초기 수수료 설정 이력 생성 중 오류 발생", e);
            }
        }
    }

    /**
     * 화물 등록 시 실제 계산에 사용할 수수료율을 조회합니다.
     * DB 값이 비정상적이거나 누락된 경우 기본값(0.1)을 반환하여 시스템 오류를 방지합니다.
     *
     * @return 현재 적용 중인 수수료율 (BigDecimal)
     */
    @Override
    public BigDecimal getFeeRateOrDefault() {
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

    /**
     * 관리자 페이지에서 현재 수수료 설정을 조회합니다.
     *
     * @param adminId 관리자 ID
     * @return 현재 설정 정보 DTO
     */
    @Override
    public AdminFeeSettingResponse getFeeSetting(Long adminId) {
        validateAdminAccess(adminId);

        SystemSetting setting = systemSettingRepository.findById(FEE_SETTING_ID)
                .orElseGet(() -> new SystemSetting(FEE_SETTING_ID, DEFAULT_FEE_RATE.toPlainString()));

        return AdminFeeSettingResponse.builder()
                .settingId(setting.getSettingId())
                .value(setting.getValue())
                .effectiveFeeRate(getFeeRateOrDefault())
                .build();
    }

    /**
     * 관리자가 수수료율을 수정합니다. 수정 시 변경 이력(EventLog)을 함께 저장합니다.
     *
     * @param adminId 수정 작업을 수행하는 관리자 ID
     * @param value 새 수수료율 문자열 (예: "0.15")
     * @return 업데이트된 설정 정보 DTO
     */
    @Override
    @Transactional
    public AdminFeeSettingResponse updateFeeSetting(Long adminId, String value) {
        Admin admin = getAdminOrThrow(adminId);

        // 입력값 유효성 검사 (숫자 여부 및 범위 0~1 확인)
        BigDecimal feeRate = parseFeeRate(value)
                .orElseThrow(() -> new IllegalArgumentException("수수료율 값은 숫자 문자열이어야 합니다."));
        if (!isInRange(feeRate)) {
            throw new IllegalArgumentException("수수료율은 0 이상 1 이하여야 합니다.");
        }

        // 불필요한 0 제거 및 정규화
        String normalizedValue = feeRate.stripTrailingZeros().toPlainString();

        SystemSetting setting = systemSettingRepository.findById(FEE_SETTING_ID)
                .orElseGet(() -> new SystemSetting(FEE_SETTING_ID, normalizedValue));

        String beforeValue = setting.getValue();
        setting.updateValue(normalizedValue);
        systemSettingRepository.save(setting);

        // 변경 이력 로그 저장 (감사 로그)
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

    /**
     * 수수료율 변경 이력 목록을 조회합니다.
     *
     * @param adminId 관리자 ID
     * @return 변경 이력 리스트
     */
    @Override
    public List<AdminFeeChangeHistoryResponse> getFeeSettingHistory(Long adminId) {
        validateAdminAccess(adminId);

        return eventLogRepository.findByEventTypeOrderByCreatedAtDesc(AdminEventType.FEE_RATE_CHANGED)
                .stream()
                .map(this::toFeeHistoryResponse)
                .toList();
    }

    /**
     * 최근 2주간의 일별 수수료율 변동 그래프 데이터를 생성합니다.
     * 특정 날짜에 변경 사항이 없으면 이전 날짜의 설정값을 유지하여 연속적인 데이터를 제공합니다.
     *
     * @param adminId 관리자 ID
     * @return 그래프 데이터 DTO
     */
    @Override
    public AdminFeeGraphResponse getFeeRateGraphForLast2Weeks(Long adminId) {
        validateAdminAccess(adminId);

        LocalDate toDate = LocalDate.now(KST);
        LocalDate fromDate = toDate.minusDays(GRAPH_DAYS - 1L);
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.plusDays(1).atStartOfDay().minusNanos(1);

        // 1. 해당 기간 내의 모든 수수료 변경 로그 조회
        List<EventLog> events = eventLogRepository.findByEventTypeAndCreatedAtBetweenOrderByCreatedAtAsc(
                AdminEventType.FEE_RATE_CHANGED,
                fromDateTime,
                toDateTime
        );

        // 2. 조회 기간 시작점(fromDate) 이전의 마지막 수수료율 조회 (초기값 설정용)
        BigDecimal seed = eventLogRepository
                .findTopByEventTypeAndCreatedAtLessThanOrderByCreatedAtDesc(AdminEventType.FEE_RATE_CHANGED, fromDateTime)
                .map(this::extractAfterValue)
                .orElse(DEFAULT_FEE_RATE); // getFeeRateOrDefault()를 호출하면 '현재' 값을 가져오므로 절대 기본값으로 고정

        // 3. 일자별 마지막 변경값 매핑 (한 날짜에 여러 번 변경 시 마지막 값 채택)
        Map<LocalDate, BigDecimal> dailyLastRate = new LinkedHashMap<>();
        for (EventLog event : events) {
            BigDecimal afterValue = extractAfterValue(event);
            if (afterValue == null) {
                continue;
            }
            LocalDate changedDate = event.getCreatedAt().atZone(KST).toLocalDate();
            dailyLastRate.put(changedDate, afterValue);
        }

        // 4. 기간 내 모든 날짜에 대해 포인트 생성 (변경 없는 날은 전일 값 유지)
        List<AdminFeeGraphPointResponse> points = new ArrayList<>();
        BigDecimal current = seed;
        for (int i = 0; i < GRAPH_DAYS; i++) {
            LocalDate date = fromDate.plusDays(i);
            boolean changed = dailyLastRate.containsKey(date);
            if (changed) {
                current = dailyLastRate.get(date);
            }
            points.add(AdminFeeGraphPointResponse.builder()
                    .date(date.format(DATE_FORMATTER))
                    .feeRate(current)
                    .changed(changed)
                    .build());
        }

        return AdminFeeGraphResponse.builder()
                .fromDate(fromDate.format(DATE_FORMATTER))
                .toDate(toDate.format(DATE_FORMATTER))
                .unit("DAY")
                .points(points)
                .build();
    }

    /**
     * EventLog 엔티티를 관리자용 이력 응답 DTO로 변환합니다.
     */
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
                .payload(toMap(payload))
                .build();
    }

    /**
     * JSON Payload에서 특정 키의 텍스트 값을 추출합니다.
     */
    private String payloadText(JsonNode payload, String key) {
        if (payload == null || !payload.hasNonNull(key)) {
            return null;
        }
        return payload.get(key).asText();
    }

    /**
     * Jackson JsonNode를 Map 구조로 변환합니다.
     */
    private Map<String, Object> toMap(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        payload.fields().forEachRemaining(entry -> result.put(entry.getKey(), toPlainValue(entry.getValue())));
        return result;
    }

    /**
     * JsonNode의 각 타입을 Java 기본 타입으로 변환합니다.
     */
    private Object toPlainValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> map.put(entry.getKey(), toPlainValue(entry.getValue())));
            return map;
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            node.forEach(item -> list.add(toPlainValue(item)));
            return list;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isIntegralNumber()) {
            return node.longValue();
        }
        if (node.isFloatingPointNumber()) {
            return node.decimalValue();
        }
        return node.asText();
    }

    /**
     * 수수료율 변경 시 로그에 저장할 JSON Payload를 빌드합니다.
     */
    private JsonNode buildFeeChangePayload(String settingId, String beforeValue, String afterValue) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("settingId", settingId);
        payload.put("beforeValue", beforeValue);
        payload.put("afterValue", afterValue);
        payload.put("effectiveFeeRate", afterValue);
        payload.put("changed", true);
        return payload;
    }

    /**
     * 이벤트 로그의 Payload에서 'afterValue'를 추출하여 BigDecimal로 파싱합니다.
     */
    private BigDecimal extractAfterValue(EventLog eventLog) {
        String afterValue = payloadText(eventLog.getPayload(), "afterValue");
        if (afterValue == null || afterValue.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(afterValue.trim());
        } catch (NumberFormatException e) {
            log.warn("수수료율 변경 로그 payload 파싱에 실패해 해당 이벤트를 건너뜁니다. logId={}, afterValue={}",
                    eventLog.getLogId(), afterValue);
            return null;
        }
    }

    /**
     * 문자열 설정값을 BigDecimal로 파싱합니다.
     */
    private Optional<BigDecimal> parseFeeRate(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(rawValue.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 수수료율이 허용 범위(0~1, 즉 0%~100%) 내에 있는지 확인합니다.
     */
    private boolean isInRange(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) >= 0 && value.compareTo(BigDecimal.ONE) <= 0;
    }

    /**
     * 요청자가 유효한 관리자인지 검증합니다.
     */
    private void validateAdminAccess(Long adminId) {
        if (!adminRepository.existsById(adminId)) {
            throw new RoleAccessDeniedException("Admin access required.");
        }
    }

    /**
     * 관리자 정보를 조회하거나 없으면 예외를 발생시킵니다.
     */
    private Admin getAdminOrThrow(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new RoleAccessDeniedException("Admin access required."));
    }
}
