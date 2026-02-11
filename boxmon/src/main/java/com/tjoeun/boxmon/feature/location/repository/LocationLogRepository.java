package com.tjoeun.boxmon.feature.location.repository;

import com.tjoeun.boxmon.feature.location.domain.LocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {
}
