package com.tjoeun.boxmon.feature.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {
    CARGO("카고(오픈형)"),
    VAN("탑차(박스형)"),
    WINGBODY("윙바디"),
    TANKER("탱크로리"),
    DUMP("덤프트럭"),
    BULK("벌크차(사일로)");

    private final String description;
}