package com.tjoeun.boxmon.feature.notification.beans;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class NotificationService {
    public String sendMessage(String targetDeviceToken, String title, String body, Map<String,String> extraData) {
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
                //.setToken(targetDeviceToken)
                .setTopic("test_topic")
                .setNotification(notification)
                .putAllData(extraData) //클라이언트가 읽을 추가 데이터
                .setAndroidConfig(androidConfig)
                .setApnsConfig(apnsConfig)
                .build();

        try {
            return FirebaseMessaging.getInstance().send(message, true);
        } catch (FirebaseMessagingException e) {
            log.warn("알림 전송 실패. 원인: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
