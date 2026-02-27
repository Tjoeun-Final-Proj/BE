package com.tjoeun.boxmon.feature.settlement.impl;

import com.tjoeun.boxmon.global.client.TossApiClient;
import com.tjoeun.boxmon.feature.settlement.DriverRegisterUsecase;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements DriverRegisterUsecase {

    private final TossApiClient tossApiClient;
    
    //차주를 셀러로 등록
    @Override
    public void registerDriver(Driver driver) {
        User user = driver.getUser();
        String normalizedId = String.format("%7d",driver.getDriverId());
        tossApiClient.registerDriver(
                normalizedId,
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                driver.getBankCode(),
                driver.getAccountNumber(),
                driver.getHolderName()
        );
    }
}
