package com.tjoeun.boxmon.feature.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LogResponse {
    private Long logId;
    private String adminName;
    private String eventType;
    private String description;
    private String payloadMessage;
    private LocalDateTime createdAt;
}
