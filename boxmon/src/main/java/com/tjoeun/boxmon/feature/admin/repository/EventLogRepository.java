package com.tjoeun.boxmon.feature.admin.repository;

import com.tjoeun.boxmon.feature.admin.domain.AdminEventType;
import com.tjoeun.boxmon.feature.admin.domain.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findByEventTypeOrderByCreatedAtDesc(AdminEventType eventType);
}
