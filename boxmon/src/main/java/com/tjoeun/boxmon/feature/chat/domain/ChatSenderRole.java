package com.tjoeun.boxmon.feature.chat.domain;

import com.tjoeun.boxmon.exception.ChatValidationException;

public enum ChatSenderRole {
    SHIPPER,
    DRIVER;

    public static ChatSenderRole from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ChatValidationException("X-USER-ROLE 헤더가 필요합니다.");
        }
        try {
            return ChatSenderRole.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ChatValidationException("X-USER-ROLE 값은 SHIPPER 또는 DRIVER 이어야 합니다.");
        }
    }
}
