package com.tjoeun.boxmon.feature.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class AdminFeeChangeHistoryResponse {
    private Long logId;
    private Long adminId;
    private String adminName;
    private String eventType;
    private String eventTypeDescription;
    private LocalDateTime createdAt;
    private String beforeValue;
    private String afterValue;
    private String changedBy;
    private Map<String, Object> payload;
}
