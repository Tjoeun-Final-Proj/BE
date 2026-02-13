package com.tjoeun.boxmon.feature.notification.repository;

import com.tjoeun.boxmon.feature.notification.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {
}
