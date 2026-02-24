package com.tjoeun.boxmon.feature.admin.repository;

import com.tjoeun.boxmon.feature.admin.domain.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
