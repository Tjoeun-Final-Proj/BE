package com.tjoeun.boxmon.global.systemsetting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "system_setting")
@NoArgsConstructor
public class SystemSetting {

    @Id
    @Column(name = "setting_id", nullable = false, length = 50)
    private String settingId;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    public SystemSetting(String settingId, String value) {
        this.settingId = settingId;
        this.value = value;
    }

    public void updateValue(String value) {
        this.value = value;
    }
}
