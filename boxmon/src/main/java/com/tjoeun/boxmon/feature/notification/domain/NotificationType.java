package com.tjoeun.boxmon.feature.notification.domain;

public enum NotificationType {
    ASSIGNMENT_CONFIRMED,      // 배차완료
    TRANSPORT_STARTED,         // 운송시작
    TRANSPORT_COMPLETED,       // 운송완료
    ASSIGNMENT_CANCELLATION_REQUESTED, // 배차 취소 요청
    CHAT_MESSAGE               // 채팅 알림
}