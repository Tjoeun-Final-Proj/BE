package com.tjoeun.boxmon.feature.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class FirebaseInitializer {
    @PostConstruct
    public void initFirebaseMessaging() {
        try(InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream()) {
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            log.error("Firebase 초기화 실패. 알림 기능이 정상작동하지 않을 수 있습니다. 원인: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
