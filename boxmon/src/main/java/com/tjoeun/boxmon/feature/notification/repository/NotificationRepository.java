package com.tjoeun.boxmon.feature.notification.repository;

import com.tjoeun.boxmon.feature.notification.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByTarget_UserId(Long userId);
}
