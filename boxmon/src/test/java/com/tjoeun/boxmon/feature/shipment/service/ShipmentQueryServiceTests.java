package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.DriverTodaySummaryResponse;
import com.tjoeun.boxmon.feature.shipment.mapper.ShipmentMapper;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.global.naver.api.NaverDirectionsApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentQueryServiceTests {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private NaverDirectionsApiClient naverDirectionsApiClient;

    @Mock
    private ShipmentDomainSupport support;

    @Mock
    private ShipmentMapper shipmentMapper;

    @InjectMocks
    private ShipmentQueryService shipmentQueryService;

    @Test
    @DisplayName("차주 오늘 요약 조회 시 오늘 운행/첫 상차/운송중 건수를 반환한다")
    void getMyDriverTodaySummary_returnsSummary() {
        Long driverId = 1L;
        LocalDateTime firstPickup = LocalDate.now(ZoneId.of("Asia/Seoul")).atTime(10, 30);
        Shipment firstShipment = Shipment.builder()
                .pickupDesiredAt(firstPickup)
                .build();

        when(shipmentRepository.countByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(3L);
        when(shipmentRepository.findFirstByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusInOrderByPickupDesiredAtAsc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(Optional.of(firstShipment));
        when(shipmentRepository.countByDriver_DriverIdAndShipmentStatus(driverId, ShipmentStatus.IN_TRANSIT))
                .thenReturn(1L);

        DriverTodaySummaryResponse response = shipmentQueryService.getMyDriverTodaySummary(driverId);

        assertEquals(3, response.getTodayScheduleCount());
        assertEquals(firstPickup, response.getFirstPickupDesiredAt());
        assertEquals(1, response.getInTransitCount());
        verify(support).validateDriverAccess(driverId);
    }

    @Test
    @DisplayName("오늘 대상 건이 없으면 첫 상차 시간은 null을 반환한다")
    void getMyDriverTodaySummary_returnsNullFirstPickupWhenNoSchedule() {
        Long driverId = 2L;

        when(shipmentRepository.countByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(0L);
        when(shipmentRepository.findFirstByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusInOrderByPickupDesiredAtAsc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(Optional.empty());
        when(shipmentRepository.countByDriver_DriverIdAndShipmentStatus(driverId, ShipmentStatus.IN_TRANSIT))
                .thenReturn(0L);

        DriverTodaySummaryResponse response = shipmentQueryService.getMyDriverTodaySummary(driverId);

        assertEquals(0, response.getTodayScheduleCount());
        assertNull(response.getFirstPickupDesiredAt());
        assertEquals(0, response.getInTransitCount());
    }

    @Test
    @DisplayName("오늘 운행 집계 상태는 ASSIGNED와 IN_TRANSIT만 사용한다")
    void getMyDriverTodaySummary_usesAssignedAndInTransitForTodaySchedule() {
        Long driverId = 3L;
        ArgumentCaptor<List<ShipmentStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);

        when(shipmentRepository.countByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(0L);
        when(shipmentRepository.findFirstByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusInOrderByPickupDesiredAtAsc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(Optional.empty());
        when(shipmentRepository.countByDriver_DriverIdAndShipmentStatus(driverId, ShipmentStatus.IN_TRANSIT))
                .thenReturn(0L);

        shipmentQueryService.getMyDriverTodaySummary(driverId);

        verify(shipmentRepository).countByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), statusesCaptor.capture());
        assertEquals(List.of(ShipmentStatus.ASSIGNED, ShipmentStatus.IN_TRANSIT), statusesCaptor.getValue());
    }

    @Test
    @DisplayName("현재 운송 중 건수는 상태 기반 단일 count 쿼리로 집계한다")
    void getMyDriverTodaySummary_countsInTransitByStatusOnly() {
        Long driverId = 4L;

        when(shipmentRepository.countByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusIn(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(1L);
        when(shipmentRepository.findFirstByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusInOrderByPickupDesiredAtAsc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(Optional.empty());
        when(shipmentRepository.countByDriver_DriverIdAndShipmentStatus(driverId, ShipmentStatus.IN_TRANSIT))
                .thenReturn(2L);

        shipmentQueryService.getMyDriverTodaySummary(driverId);

        verify(shipmentRepository).countByDriver_DriverIdAndShipmentStatus(driverId, ShipmentStatus.IN_TRANSIT);
    }
}
