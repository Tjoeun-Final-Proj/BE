package com.tjoeun.boxmon.feature.settlement.service;

import com.tjoeun.boxmon.feature.user.domain.Driver;

public interface DriverRegisterUsecase {
    /**
     * 차주를 셀러로 등록(계좌정보가 필요하므로 계좌 등록 후 호출)
     * @param driver 등록할 차주
     */
    void registerDriver(Driver driver);
}
