package com.tjoeun.boxmon.feature.user.repository;

import com.tjoeun.boxmon.feature.user.domain.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUser_UserId(Long userId);
}
