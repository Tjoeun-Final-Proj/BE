package com.tjoeun.boxmon.feature.settlement.service.impl;

import com.tjoeun.boxmon.feature.settlement.domain.Settlement;
import com.tjoeun.boxmon.feature.settlement.repository.SettlementRepository;
import com.tjoeun.boxmon.feature.settlement.service.DriverRegisterUsecase;
import com.tjoeun.boxmon.feature.settlement.service.SettlementNotifier;
import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.global.client.TossApiClient;
import com.tjoeun.boxmon.global.util.SystemSettingProvider;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements DriverRegisterUsecase, SettlementNotifier {

    private final TossApiClient tossApiClient;
    private final EntityManager em;
    private final SystemSettingProvider systemSettingProvider;
    private final SettlementRepository settlementRepository;

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
    
    @Override
    public void onShipmentCompleted(Long shipmentId) {
        SystemSettingProvider.NextSettlementTime settlementTimeSetting = systemSettingProvider.getNextSettlementTime();
        
        Settlement settlement = Settlement.builder()
                .shipment(em.getReference(Shipment.class, shipmentId))
                .settlementStatus(SettlementStatus.PENDING)
                .settleScheduledAt(settlementTimeSetting.nextSettlementTime())
                .policyVersion(settlementTimeSetting.policyVersion())
                .build();
        
        try{
            settlementRepository.save(settlement);
        }
        catch (DataIntegrityViolationException e){
            log.warn("운송 완료 신호가 중복되어 들어왔습니다. 나중에 들어온 신호를 무시합니다.");
        }
    }
}
