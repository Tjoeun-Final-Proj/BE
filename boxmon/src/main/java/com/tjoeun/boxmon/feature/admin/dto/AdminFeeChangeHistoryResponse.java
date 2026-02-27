package com.tjoeun.boxmon.feature.admin.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
    private JsonNode payload;
}

