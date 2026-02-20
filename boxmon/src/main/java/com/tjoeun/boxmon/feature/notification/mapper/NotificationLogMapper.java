package com.tjoeun.boxmon.feature.notification.mapper;

import com.tjoeun.boxmon.feature.notification.domain.NotificationLog;
import com.tjoeun.boxmon.feature.notification.dto.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogMapper {
    public NotificationResponse toResponse(NotificationLog notificationLog){
        return NotificationResponse.builder()
                .notificationId(notificationLog.getNotificationId())
                .shipmentId(notificationLog.getShipment().getShipmentId())
                .notificationType(notificationLog.getNotificationType())
                .notificationTitle(notificationLog.getNotificationTitle())
                .notificationContent(notificationLog.getNotificationContent())
                .build();
    }
}
