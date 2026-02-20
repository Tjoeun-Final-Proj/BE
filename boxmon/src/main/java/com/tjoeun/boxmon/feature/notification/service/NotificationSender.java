package com.tjoeun.boxmon.feature.notification.service;

import com.google.firebase.messaging.*;
import com.tjoeun.boxmon.feature.notification.domain.NotificationLog;
import com.tjoeun.boxmon.feature.notification.domain.NotificationType;
import com.tjoeun.boxmon.feature.notification.repository.NotificationRepository;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSender {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    //알림 전송 + 알림목록 갱신
    public void send(long targetUserId, long shipmentId, NotificationType type, String title, String body, Map<String,String> extraData) {
        try {
            User target = userRepository.findByUserId(targetUserId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            send(target, shipmentId, type, title, body, extraData);
        } catch (IllegalArgumentException e) {
            log.warn("알림 전송 실패. 알림을 보낼 사용자를 찾을 수 없습니다. 사용자 id: {}", targetUserId);
        }
    }
    
    //알림 전송 + 알림목록 갱신
    public void send(User target, long shipmentId, NotificationType type, String title, String body, Map<String,String> extraData){
        if(extraData == null) extraData = Map.of("shipmentId",String.valueOf(shipmentId));
        
        String messageId = sendMessage(target.getDeviceToken(), title, body, extraData);
        NotificationLog sentMessage = NotificationLog.builder()
                .target(target)
                .shipment(entityManager.getReference(Shipment.class, shipmentId))
                .notificationType(type)
                .notificationTitle(title)
                .notificationContent(body)
                .sentAt(LocalDateTime.now())
                .firebaseMessageId(messageId)
                .build();
        notificationRepository.save(sentMessage);
    }
    
    //FCM을 이용한 실질적인 메시지 전송
    private String sendMessage(String targetDeviceToken, String title, String body, Map<String,String> extraData) {
        //푸시 알림에 띄울 내용
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        //Android 전용 추가 설정
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .build())
                .build();


        //IOS 전용 추가 설정
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .build()
                )
                .build();
        
        //전송할 메시지 객체
        Message message = Message.builder()
                .setToken(targetDeviceToken)
                .setNotification(notification)
                .putAllData(extraData) //클라이언트가 읽을 추가 데이터
                .setAndroidConfig(androidConfig)
                .setApnsConfig(apnsConfig)
                .build();

        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("알림 전송 실패. 원인: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
