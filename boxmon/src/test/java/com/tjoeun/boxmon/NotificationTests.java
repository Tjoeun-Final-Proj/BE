package com.tjoeun.boxmon;

import com.tjoeun.boxmon.feature.notification.service.NotificationSender;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

@SpringBootTest
@TestPropertySource(properties = {
        "logging.level.com.tjoeun.boxmon.feature.notification=DEBUG"
})
public class NotificationTests {
    private static final Logger log = LoggerFactory.getLogger(NotificationTests.class);
    @Autowired
    NotificationSender notificationService;
    
    @Test
    public void testNotification() {
        notificationService.send(
                1L, 
                "테스트 메시지 제목",
                "테스트 메시지 내용aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                Map.of("content","test payload")
        );
    }
}
