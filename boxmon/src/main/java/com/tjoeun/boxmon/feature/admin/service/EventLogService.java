package com.tjoeun.boxmon.feature.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper; // 추가
import com.tjoeun.boxmon.feature.admin.domain.EventLog;
import com.tjoeun.boxmon.feature.admin.dto.LogResponse;
import com.tjoeun.boxmon.feature.admin.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventLogService {

    private final EventLogRepository eventLogRepository;
    // Autowire 에러가 난다면 여기서 직접 생성
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<LogResponse> getAllLogs() {
        // Repository에서 반환 타입을 EventLog로 명확히 해야 합니다.
        return eventLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LogResponse toResponse(EventLog log) {
        // payload가 JsonNode라면 asText(), 아니라면 String 변환 로직
        String message = log.getPayload() != null ? log.getPayload().asText() : "";

        return LogResponse.builder()
                .logId(log.getLogId())
                .adminName(log.getAdmin().getName())
                .eventType(log.getEventType().name())
                .description(log.getEventType().getDescription())
                .payloadMessage(message)
                .createdAt(log.getCreatedAt())
                .build();
    }
}