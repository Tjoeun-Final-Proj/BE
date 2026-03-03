package com.tjoeun.boxmon.feature.admin.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminEventType {
    ADMIN_ACCOUNT_CREATED("관리자 계정 생성"),
    SHIPMENT_FORCE_CANCELED("운송 강제 취소"),
    INQUIRY_ANSWERED("문의 답변"),
    FEE_RATE_CHANGED("수수료율 변경");

    private final String description;
}

