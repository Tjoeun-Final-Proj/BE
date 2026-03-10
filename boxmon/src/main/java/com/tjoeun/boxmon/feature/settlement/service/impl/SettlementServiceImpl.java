package com.tjoeun.boxmon.feature.settlement.service.impl;

import com.tjoeun.boxmon.exception.ConcurrentRequestExceedException;
import com.tjoeun.boxmon.exception.ExternalServiceException;
import com.tjoeun.boxmon.exception.RateLimitExceededException;
import com.tjoeun.boxmon.feature.payment.repository.PaymentRepository;
import com.tjoeun.boxmon.feature.settlement.domain.Settlement;
import com.tjoeun.boxmon.feature.settlement.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.settlement.dto.SettlementRequest;
import com.tjoeun.boxmon.feature.settlement.exception.SettlementConflictException;
import com.tjoeun.boxmon.feature.settlement.repository.SettlementRepository;
import com.tjoeun.boxmon.feature.settlement.service.DriverRegisterUseCase;
import com.tjoeun.boxmon.feature.settlement.service.DriverSettlementUseCase;
import com.tjoeun.boxmon.feature.settlement.service.SettlementNotifier;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.repository.DriverRepository;
import com.tjoeun.boxmon.global.toss.client.TossApiClient;
import com.tjoeun.boxmon.global.util.SystemSettingProvider;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements DriverRegisterUseCase, SettlementNotifier, DriverSettlementUseCase {

    private final TossApiClient tossApiClient;
    private final EntityManager em;
    private final SystemSettingProvider systemSettingProvider;
    private final SettlementRepository settlementRepository;
    private final PaymentRepository paymentRepository;
    private final DriverRepository driverRepository;
    private final ShipmentRepository shipmentRepository;

    //차주를 셀러로 등록
    @Override
    public String registerDriver(Driver driver) {
        User user = driver.getUser();
        String normalizedId = String.format("%07d",driver.getDriverId());
        return tossApiClient.registerDriver(
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
    
    //정산 요청 처리
    @Override
    @Transactional
    public void requestSettlement(SettlementRequest request) {
        //정산중 상태 선점 시도
        Long shipmentId = request.getShipmentId();
        int updatedRRow = settlementRepository.saveSettlementProgressByShipmentId(shipmentId);
        if(updatedRRow != 1) { //선점 실패시 처리
            Optional<Settlement> optionalSettlement = settlementRepository.findByShipment_ShipmentId(shipmentId);
            if(optionalSettlement.isEmpty()) 
                throw new IllegalArgumentException("아직 운송이 완료되지 않았습니다.");
            
            switch (optionalSettlement.get().getSettlementStatus()) {
                case PENDING -> throw new SettlementConflictException("일시적인 오류입니다. 잠시후 다시 시도해주세요.");
                case PROGRESS -> throw new ConcurrentRequestExceedException("이미 정산 처리 중입니다.");
                case PAID -> throw new SettlementConflictException("이미 정산이 완료되었습니다.");
                case HOLD -> throw new SettlementConflictException("관리자에 의해 정산이 보류되었습니다. 관리자에게 문의해주세요.");
            }
        }

        //마지막 정산 확인 시간으로부터 1시간 이상 지났는지 검증(정산일이 밀린 경우 중복 확인을 지원하되 빈도 제한을 걸기 위해 사용)
        Settlement settlement = settlementRepository.findByShipment_ShipmentId(shipmentId)
                .orElseThrow(() -> new IllegalStateException("처리 중이던 정산 상태 데이터가 사라짐"));
        LocalDateTime lastCheckTime = settlement.getLastCheckAt();
        boolean isRecentlyChecked = lastCheckTime != null && LocalDateTime.now().isAfter(lastCheckTime.plusHours(1L));
        if(isRecentlyChecked) {
            throw new RateLimitExceededException(
                    "토스 서버 점검으로 인해 정산이 지연되고 있습니다. 1~2시간 후 다시 시도해주세요.",
                    String.valueOf(Duration.between(lastCheckTime.plusHours(1L),LocalDateTime.now()).getSeconds())
            );
        }
        
        //정산 예정일이 지났는지 확인
        if(LocalDateTime.now().isBefore(settlement.getSettleScheduledAt())){ //정산 예정일이 아직 안 지났으면
            throw new SettlementConflictException("정산 가능 시점 이전입니다.");
        }
        
        //플랫폼 정산 완료 여부 확인
        if(!tossApiClient.checkSettled(String.format("%06d", shipmentId))) {
            settlement.setLastCheckAt(LocalDateTime.now());
            throw new SettlementConflictException("토스 서버 점검으로 인해 정산이 지연되고 있습니다. 1~2시간 후 다시 시도해주세요.");
        }
        
        //지급대행 api 호출
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(()->new IllegalArgumentException("대상 운송건을 찾을 수 없습니다."));
        try {
            tossApiClient.settleDriver(
                    String.format("%d", shipmentId),
                    shipment.getDriver().getTossSellerId(),
                    shipment.getProfit()
            );
        }
        catch (ExternalServiceException e){
            settlement.failed();
            settlementRepository.save(settlement);
            throw e;
        }

        settlement.complete();
        settlementRepository.save(settlement);
    }
}
