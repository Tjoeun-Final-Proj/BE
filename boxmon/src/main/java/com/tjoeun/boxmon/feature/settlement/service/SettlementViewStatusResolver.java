package com.tjoeun.boxmon.feature.settlement.service;

import com.tjoeun.boxmon.feature.settlement.domain.Settlement;
import com.tjoeun.boxmon.feature.settlement.dto.SettlementViewStatus;
import com.tjoeun.boxmon.feature.settlement.repository.SettlementRepository;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettlementViewStatusResolver {
    private final SettlementRepository settlementRepository;

    //정산상태 변환용(토스 api를 사용하지 않고 DB 조회로만 처리, 실제 유효성은 정산 요청 시에 정산 모듈이 별도 확인)
    public SettlementViewStatus resolve(Shipment shipment) {
        //정산 상태 확인
        Optional<Settlement> optionalSettlement = settlementRepository.findByShipment(shipment);
        
        //정산 상태가 저장되지 않은 경우(운송상태가 배차완료~운송중인 경우)
        if(optionalSettlement.isEmpty())
            return SettlementViewStatus.INELIGIBLE; //정산불가
        
        //정산 예정일이 지나지 않은 경우(플랫폼이 PG사(토스)로 부터 아직 정산을 받지 못한 경우)
        Settlement settlement = optionalSettlement.get();
        if(settlement.getSettleScheduledAt().isAfter(LocalDateTime.now())){
            return SettlementViewStatus.INELIGIBLE;
        }
        
        return switch (settlement.getSettlementStatus()) {
            case PENDING -> SettlementViewStatus.READY;
            case PROGRESS -> SettlementViewStatus.PROGRESS;
            case PAID -> SettlementViewStatus.PAID;
            case HOLD -> SettlementViewStatus.INELIGIBLE;
        };
    }
}
