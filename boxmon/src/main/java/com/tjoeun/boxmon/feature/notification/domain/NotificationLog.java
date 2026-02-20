package com.tjoeun.boxmon.feature.notification.domain;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
    
    @Column(name = "notification_title", nullable = false)
    private String notificationTitle;
    
    @Column(name = "notification_content", nullable = false)
    private String notificationContent;
    
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "firebase_message_id")
    private String firebaseMessageId;

    @Builder
    public NotificationLog(User target, Shipment shipment, NotificationType notificationType, String notificationTitle, String notificationContent, String extraData, LocalDateTime sentAt, String firebaseMessageId) {
        this.target = target;
        this.shipment = shipment;
        this.notificationType = notificationType;
        this.notificationTitle = notificationTitle;
        this.notificationContent = notificationContent;
        this.extraData = extraData;
        this.sentAt = sentAt;
        this.firebaseMessageId = firebaseMessageId;
    }
}
