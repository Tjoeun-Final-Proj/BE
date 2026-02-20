package com.tjoeun.boxmon.feature.notification.dto;

import com.tjoeun.boxmon.feature.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationResponse {
    private long notificationId;
    private long shipmentId;
    private NotificationType notificationType;
    private String notificationTitle;
    private String notificationContent;
    private Map<String,String> extraData;
}
