package com.tjoeun.boxmon.feature.notification.controller;

import com.tjoeun.boxmon.feature.notification.dto.NotificationResponse;
import com.tjoeun.boxmon.feature.notification.service.NotificationReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationReadService notificationService;

    @GetMapping("/list")
    public ResponseEntity<List<NotificationResponse>> listNotifications(@AuthenticationPrincipal Long userId) {
        List<NotificationResponse> list = notificationService.getListOf(userId);
        return ResponseEntity.ok(list);
    }
}
