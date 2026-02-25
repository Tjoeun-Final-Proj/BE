package com.tjoeun.boxmon.feature.admin.dto;

import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.user.domain.User;
import lombok.Getter;

@Getter
public class PenaltiesDto {

    private Long adminId;

    private Long userId;

    private String payload;
}
