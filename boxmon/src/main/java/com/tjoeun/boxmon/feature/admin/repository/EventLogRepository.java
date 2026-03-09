package com.tjoeun.boxmon.feature.admin.repository;

import com.tjoeun.boxmon.feature.admin.domain.AdminEventType;
import com.tjoeun.boxmon.feature.admin.domain.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findByEventTypeOrderByCreatedAtDesc(AdminEventType eventType);
    List<EventLog> findByEventTypeAndCreatedAtBetweenOrderByCreatedAtAsc(
            AdminEventType eventType,
            LocalDateTime from,
            LocalDateTime to
    );
    Optional<EventLog> findTopByEventTypeAndCreatedAtLessThanOrderByCreatedAtDesc(
            AdminEventType eventType,
            LocalDateTime before
    );
    List<EventLog> findAllByOrderByCreatedAtDesc();

}
