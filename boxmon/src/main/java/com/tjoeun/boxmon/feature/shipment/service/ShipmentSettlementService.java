package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.exception.ShipmentStateConflictException;
import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.DriverSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.DriverSettlementSummaryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperSettlementSummaryResponse;
import com.tjoeun.boxmon.feature.shipment.mapper.ShipmentMapper;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * Shipment 정산 계열을 담당하는 서비스.
 * 정산 요약/목록/상세 조회와 정산 접근 권한 검증을 처리합니다.
 */
public class ShipmentSettlementService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentDomainSupport support;
    private final ShipmentMapper shipmentMapper;

    /**
     * 정산 상세 조회: DONE 상태 + 화주/배정차주 본인만 허용.
     */
    public ShipmentDetailResponse getSettlementShipmentDetail(Long userId, Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("배송을 찾을 수 없습니다."));

        if (shipment.getShipmentStatus() != ShipmentStatus.DONE) {
            throw new ShipmentStateConflictException("Only completed shipments can be viewed in settlement detail.");
        }

        boolean isShipper = shipment.getShipper() != null && shipment.getShipper().getShipperId().equals(userId);
        boolean isDriver = shipment.getDriver() != null && shipment.getDriver().getDriverId().equals(userId);

        if (!isShipper && !isDriver) {
            throw new RoleAccessDeniedException("Only shipment shipper or assigned driver can view this settlement detail.");
        }

        return shipmentMapper.toDetailResponse(shipment, true, true);
    }

    /**
     * 화주 정산 요약(이번 달/지난 달/차액).
     */
    public ShipperSettlementSummaryResponse getShipperSettlementSummary(Long shipperId) {
        support.validateShipperAccess(shipperId);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusNanos(1);

        BigDecimal thisMonthTotal = Optional.ofNullable(
                shipmentRepository.findTotalAmountByShipperAndPeriod(shipperId, startOfThisMonth, now)
        ).orElse(BigDecimal.ZERO);

        BigDecimal lastMonthTotal = Optional.ofNullable(
                shipmentRepository.findTotalAmountByShipperAndPeriod(shipperId, startOfLastMonth, endOfLastMonth)
        ).orElse(BigDecimal.ZERO);

        return ShipperSettlementSummaryResponse.builder()
                .thisMonthTotalAmount(shipmentMapper.roundMoney(thisMonthTotal))
                .lastMonthTotalAmount(shipmentMapper.roundMoney(lastMonthTotal))
                .difference(shipmentMapper.roundMoney(thisMonthTotal.subtract(lastMonthTotal)))
                .build();
    }

    /**
     * 차주 정산 요약(이번 달/지난 달/차액).
     */
    public DriverSettlementSummaryResponse getDriverSettlementSummary(Long driverId) {
        support.validateDriverAccess(driverId);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusNanos(1);

        BigDecimal thisMonthProfit = Optional.ofNullable(
                shipmentRepository.findTotalProfitByDriverAndPeriod(driverId, startOfThisMonth, now)
        ).orElse(BigDecimal.ZERO);

        BigDecimal lastMonthProfit = Optional.ofNullable(
                shipmentRepository.findTotalProfitByDriverAndPeriod(driverId, startOfLastMonth, endOfLastMonth)
        ).orElse(BigDecimal.ZERO);

        return DriverSettlementSummaryResponse.builder()
                .thisMonthTotalProfit(shipmentMapper.roundMoney(thisMonthProfit))
                .lastMonthTotalProfit(shipmentMapper.roundMoney(lastMonthProfit))
                .difference(shipmentMapper.roundMoney(thisMonthProfit.subtract(lastMonthProfit)))
                .build();
    }

    /**
     * 화주 정산 목록 조회(연월 + 선택 필터).
     */
    public List<ShipperSettlementListResponse> getShipperSettlementList(
            Long shipperId,
            int year,
            int month,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        support.validateShipperAccess(shipperId);
        support.validateYearMonth(year, month);

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        List<Shipment> shipments = findShipperSettlementShipments(
                shipperId, start, end, shipmentStatus, settlementStatus
        );

        return shipments.stream()
                .map(shipmentMapper::toShipperSettlementListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 차주 정산 목록 조회(연월 + 선택 필터).
     */
    public List<DriverSettlementListResponse> getDriverSettlementList(
            Long driverId,
            int year,
            int month,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        support.validateDriverAccess(driverId);
        support.validateYearMonth(year, month);

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        List<Shipment> shipments = findDriverSettlementShipments(
                driverId, start, end, shipmentStatus, settlementStatus
        );

        return shipments.stream()
                .map(shipmentMapper::toDriverSettlementListResponse)
                .collect(Collectors.toList());
    }

    private List<Shipment> findShipperSettlementShipments(
            Long shipperId,
            LocalDateTime start,
            LocalDateTime end,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        if (shipmentStatus != null && settlementStatus != null) {
            return shipmentRepository
                    .findByShipper_ShipperIdAndCreatedAtBetweenAndShipmentStatusAndSettlementStatusOrderByCreatedAtDesc(
                            shipperId, start, end, shipmentStatus, settlementStatus
                    );
        }
        if (shipmentStatus != null) {
            return shipmentRepository
                    .findByShipper_ShipperIdAndCreatedAtBetweenAndShipmentStatusOrderByCreatedAtDesc(
                            shipperId, start, end, shipmentStatus
                    );
        }
        if (settlementStatus != null) {
            return shipmentRepository
                    .findByShipper_ShipperIdAndCreatedAtBetweenAndSettlementStatusOrderByCreatedAtDesc(
                            shipperId, start, end, settlementStatus
                    );
        }
        return shipmentRepository
                .findByShipper_ShipperIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        shipperId, start, end
                );
    }

    private List<Shipment> findDriverSettlementShipments(
            Long driverId,
            LocalDateTime start,
            LocalDateTime end,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        if (shipmentStatus != null && settlementStatus != null) {
            return shipmentRepository
                    .findByDriver_DriverIdAndCreatedAtBetweenAndShipmentStatusAndSettlementStatusOrderByCreatedAtDesc(
                            driverId, start, end, shipmentStatus, settlementStatus
                    );
        }
        if (shipmentStatus != null) {
            return shipmentRepository
                    .findByDriver_DriverIdAndCreatedAtBetweenAndShipmentStatusOrderByCreatedAtDesc(
                            driverId, start, end, shipmentStatus
                    );
        }
        if (settlementStatus != null) {
            return shipmentRepository
                    .findByDriver_DriverIdAndCreatedAtBetweenAndSettlementStatusOrderByCreatedAtDesc(
                            driverId, start, end, settlementStatus
                    );
        }
        return shipmentRepository
                .findByDriver_DriverIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        driverId, start, end
                );
    }
}
