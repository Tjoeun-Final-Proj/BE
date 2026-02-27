package com.tjoeun.boxmon.global.systemsetting.repository;

import com.tjoeun.boxmon.global.systemsetting.domain.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
