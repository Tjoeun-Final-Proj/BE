package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.dto.DriverTodaySummaryResponse;
import com.tjoeun.boxmon.feature.shipment.mapper.ShipmentMapper;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.global.naver.api.NaverDirectionsApiClient;
import com.tjoeun.boxmon.global.naver.dto.NaverDirectionsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentQueryServiceTests {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

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

    @Test
    @DisplayName("배차 수락 상세 조회 시 전체 경로 기반 예상 거리와 도착 시간을 반환한다")
    void getShipmentAcceptDetail_returnsDistanceAndEstimatedArrivalTime() {
        Long shipmentId = 10L;
        LocalDateTime pickupDesiredAt = LocalDateTime.of(2026, 3, 11, 9, 0);
        Shipment shipment = Shipment.builder()
                .shipmentId(shipmentId)
                .pickupDesiredAt(pickupDesiredAt)
                .pickupPoint(GEOMETRY_FACTORY.createPoint(new Coordinate(127.1000, 37.5000)))
                .dropoffPoint(GEOMETRY_FACTORY.createPoint(new Coordinate(128.1000, 37.6000)))
                .waypoint1Point(GEOMETRY_FACTORY.createPoint(new Coordinate(127.5000, 37.5500)))
                .waypoint2Point(GEOMETRY_FACTORY.createPoint(new Coordinate(127.8000, 37.5800)))
                .build();
        ShipmentDetailResponse mappedResponse = new ShipmentDetailResponse();
        NaverDirectionsResponse directionsResponse = createDirectionsResponse(12500, 5400000);

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentMapper.toDetailResponse(shipment, true, true)).thenReturn(mappedResponse);
        when(naverDirectionsApiClient.getDirections(
                "127.1,37.5",
                "128.1,37.6",
                List.of("127.5,37.55", "127.8,37.58")))
                .thenReturn(Optional.of(directionsResponse));

        ShipmentDetailResponse response = shipmentQueryService.getShipmentAcceptDetail(shipmentId);

        assertEquals(mappedResponse, response);
        assertEquals("12.5", response.getDistanceToDestination());
        assertEquals(pickupDesiredAt.plus(Duration.ofMillis(5400000)), response.getEstimatedArrivalTime());
    }

    @Test
    @DisplayName("배차 수락 상세 조회 시 길찾기 응답이 없으면 예상 거리와 도착 시간은 null이다")
    void getShipmentAcceptDetail_returnsNullWhenDirectionsUnavailable() {
        Long shipmentId = 11L;
        Shipment shipment = Shipment.builder()
                .shipmentId(shipmentId)
                .pickupDesiredAt(LocalDateTime.of(2026, 3, 11, 9, 0))
                .pickupPoint(GEOMETRY_FACTORY.createPoint(new Coordinate(127.1000, 37.5000)))
                .dropoffPoint(GEOMETRY_FACTORY.createPoint(new Coordinate(128.1000, 37.6000)))
                .build();
        ShipmentDetailResponse mappedResponse = new ShipmentDetailResponse();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentMapper.toDetailResponse(shipment, true, true)).thenReturn(mappedResponse);
        when(naverDirectionsApiClient.getDirections("127.1,37.5", "128.1,37.6", List.of()))
                .thenReturn(Optional.empty());

        ShipmentDetailResponse response = shipmentQueryService.getShipmentAcceptDetail(shipmentId);

        assertEquals(mappedResponse, response);
        assertNull(response.getDistanceToDestination());
        assertNull(response.getEstimatedArrivalTime());
    }

    @Test
    @DisplayName("배차 수락 상세 조회 시 경유지 좌표를 길찾기 API에 그대로 전달한다")
    void getShipmentAcceptDetail_passesWaypointsToDirectionsApi() {
        Long shipmentId = 12L;
        Shipment shipment = Shipment.builder()
                .shipmentId(shipmentId)
                .pickupDesiredAt(LocalDateTime.of(2026, 3, 11, 9, 0))
                .pickupPoint(GEOMETRY_FACTORY.createPoint(new Coordinate(127.1000, 37.5000)))
                .dropoffPoint(GEOMETRY_FACTORY.createPoint(new Coordinate(128.1000, 37.6000)))
                .waypoint1Point(GEOMETRY_FACTORY.createPoint(new Coordinate(127.5000, 37.5500)))
                .build();
        ShipmentDetailResponse mappedResponse = new ShipmentDetailResponse();
        ArgumentCaptor<List<String>> waypointsCaptor = ArgumentCaptor.forClass(List.class);

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentMapper.toDetailResponse(shipment, true, true)).thenReturn(mappedResponse);
        when(naverDirectionsApiClient.getDirections(any(), any(), anyList()))
                .thenReturn(Optional.of(createDirectionsResponse(1000, 600000)));

        shipmentQueryService.getShipmentAcceptDetail(shipmentId);

        verify(naverDirectionsApiClient).getDirections(
                org.mockito.ArgumentMatchers.eq("127.1,37.5"),
                org.mockito.ArgumentMatchers.eq("128.1,37.6"),
                waypointsCaptor.capture());
        assertEquals(List.of("127.5,37.55"), waypointsCaptor.getValue());
        assertTrue(mappedResponse.getEstimatedArrivalTime() != null);
    }

    private NaverDirectionsResponse createDirectionsResponse(double distance, int duration) {
        NaverDirectionsResponse.Summary summary = new NaverDirectionsResponse.Summary();
        summary.setDistance(distance);
        summary.setDuration(duration);

        NaverDirectionsResponse.TrafficGuide trafast = new NaverDirectionsResponse.TrafficGuide();
        trafast.setSummary(summary);

        NaverDirectionsResponse.Route route = new NaverDirectionsResponse.Route();
        route.setTrafast(List.of(trafast));

        NaverDirectionsResponse response = new NaverDirectionsResponse();
        response.setRoute(route);
        return response;
    }
}
