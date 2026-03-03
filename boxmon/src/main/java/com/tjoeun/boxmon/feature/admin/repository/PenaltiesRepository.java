package com.tjoeun.boxmon.feature.admin.repository;

import com.tjoeun.boxmon.feature.admin.domain.Penalties;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PenaltiesRepository extends JpaRepository<Penalties, Long> {

    List<Penalties> findAllByUser_UserId(Long userId);
}
