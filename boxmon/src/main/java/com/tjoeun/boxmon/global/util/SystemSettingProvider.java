package com.tjoeun.boxmon.global.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SystemSettingProvider {
    public record NextSettlementTime(LocalDateTime nextSettlementTime, String policyVersion) { }
    
    /**
     * 다음 정산일을 반환하는 함수
     * 현재 Mock 버전으로 5분뒤의 시간을 반환함
     * @return 다음 정산일
     */
    public NextSettlementTime getNextSettlementTime() {
        return new NextSettlementTime(
                LocalDateTime.now().plusMinutes(5),
                "TEMP_V1"
        );
    }
}
