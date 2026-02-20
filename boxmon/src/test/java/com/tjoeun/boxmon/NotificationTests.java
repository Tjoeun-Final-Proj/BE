package com.tjoeun.boxmon;

import com.tjoeun.boxmon.feature.notification.domain.NotificationType;
import com.tjoeun.boxmon.feature.notification.service.NotificationSender;
import com.tjoeun.boxmon.feature.notification.service.NotificationUseCase;
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
    NotificationSender notificationSender;
    
    @Test
    public void testNotification() {
        notificationSender.send(
                1L, 
                1L,
                NotificationType.ASSIGNMENT_CANCELLATION_REQUESTED,
                "테스트 메시지 제목",
                "2/20 테스트",
                Map.of("content","test payload")
        );
    }
    
    @Autowired
    NotificationUseCase notificationUseCase;
    
    @Test
    public void testNotification2() {
        notificationUseCase.notifyAssignmentCompleted(1);
    }
}
