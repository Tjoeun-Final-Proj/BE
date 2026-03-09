package com.tjoeun.boxmon.feature.admin.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminEventType {
    ADMIN_REGISTERED("관리자 계정 생성"),
    ADMIN_DELETED("관리자 계정 삭제"),
    MEMBER_WARNED("회원 경고 처리"),
    MEMBER_WARNING_REMOVED("회원 경고 삭제"),
    MEMBER_SUSPENDED("회원 계정 정지"),
    MEMBER_RESTORED("회원 계정 복구"),
    CONTACT_ANSWERED("문의 답변"),
    FEE_RATE_CHANGED("수수료율 변경"),
    DISPATCH_CANCEL("배차 강제 취소");

    private final String description;
}

