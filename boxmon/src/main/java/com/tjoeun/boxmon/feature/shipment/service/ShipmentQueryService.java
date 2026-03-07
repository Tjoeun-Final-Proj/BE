package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.DriverInventoryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.MyUnassignedShipmentResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperInventoryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.UnassignedShipmentResponse;
import com.tjoeun.boxmon.feature.shipment.mapper.ShipmentMapper;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.global.naver.api.NaverDirectionsApiClient;
import com.tjoeun.boxmon.global.naver.dto.NaverDirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * Shipment 조회(Query) 계열을 담당하는 서비스.
 * 상세 조회, 미배차 목록 조회, ETA/거리 계산을 처리합니다.
 */
public class ShipmentQueryService {

    private final ShipmentRepository shipmentRepository;
    private final NaverDirectionsApiClient naverDirectionsApiClient;
    private final ShipmentDomainSupport support;
    private final ShipmentMapper shipmentMapper;

    /**
     * 배차 수락 화면용 상세 조회 (사진 URL 제외).
     */
    public ShipmentDetailResponse getShipmentAcceptDetail(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

        return shipmentMapper.toDetailResponse(shipment, false, false);
    }

    /**
     * 상세 조회 시 차주 위치 유무에 따라 ETA 계산 분기를 처리합니다.
     * - 차주 현재 위치 존재: 남은 경로 ETA
     * - 그 외: 전체 경로 ETA
     */
    public ShipmentDetailResponse getShipmentDetail(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

        ShipmentDetailResponse response = shipmentMapper.toDetailResponse(shipment, false, false);

        if (shipment.getDriver() != null && shipment.getCurrentLocationPoint() != null) {
            log.info("차주 배차 및 위치 정보 확인. 현재 위치를 기준으로 남은 경로 ETA 계산을 시작합니다. (Shipment ID: {})", shipmentId);
            calculateRemainingEtaAndDistance(shipment, response);
        } else {
            log.info("차주 미배차 또는 위치 정보 없음. 희망 출발 시간을 기준으로 전체 경로 ETA 계산을 시작합니다. (Shipment ID: {})", shipmentId);
            calculateTotalEtaAndDistance(shipment, response);
        }

        return response;
    }

    /**
     * 전체 미배차(REQUESTED) 목록 조회.
     */
    public List<UnassignedShipmentResponse> getUnassignedShipments() {
        List<Shipment> shipments = shipmentRepository.findByShipmentStatusOrderByCreatedAtDesc(ShipmentStatus.REQUESTED);
        return shipments.stream()
                .map(shipmentMapper::toUnassignedShipmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * 화주 본인이 등록한 미배차(REQUESTED) 목록 조회.
     */
    public List<MyUnassignedShipmentResponse> getMyUnassignedShipments(Long shipperId) {
        support.validateShipperAccess(shipperId);
        List<Shipment> shipments = shipmentRepository
                .findByShipper_ShipperIdAndShipmentStatusOrderByCreatedAtDesc(shipperId, ShipmentStatus.REQUESTED);

        return shipments.stream()
                .map(shipmentMapper::toMyUnassignedShipmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * 화주 운송 현황 조회.
     * REQUESTED(미배차) 상태를 제외한 본인 등록 화물 목록을 조회합니다.
     */
    public List<ShipperInventoryResponse> getMyShipperInventory(Long shipperId) {
        support.validateShipperAccess(shipperId);
        List<Shipment> shipments = shipmentRepository
                .findByShipper_ShipperIdAndShipmentStatusNotOrderByCreatedAtDesc(shipperId, ShipmentStatus.REQUESTED);

        return shipments.stream()
                .map(shipmentMapper::toShipperInventoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 차주 운송 현황 조회.
     * 본인에게 배차된 화물의 전체 상태 목록을 조회합니다.
     */
    public List<DriverInventoryResponse> getMyDriverInventory(Long driverId) {
        support.validateDriverAccess(driverId);
        List<Shipment> shipments = shipmentRepository
                .findByDriver_DriverIdOrderByCreatedAtDesc(driverId);

        return shipments.stream()
                .map(shipmentMapper::toDriverInventoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 희망 출발 시간을 기준으로 전체 경로 ETA/거리 계산.
     */
    private void calculateTotalEtaAndDistance(Shipment shipment, ShipmentDetailResponse response) {
        String start = shipment.getPickupPoint().getX() + "," + shipment.getPickupPoint().getY();
        String goal = shipment.getDropoffPoint().getX() + "," + shipment.getDropoffPoint().getY();
        List<String> waypoints = new ArrayList<>();
        if (shipment.getWaypoint1Point() != null) {
            waypoints.add(shipment.getWaypoint1Point().getX() + "," + shipment.getWaypoint1Point().getY());
        }
        if (shipment.getWaypoint2Point() != null) {
            waypoints.add(shipment.getWaypoint2Point().getX() + "," + shipment.getWaypoint2Point().getY());
        }

        naverDirectionsApiClient.getDirections(start, goal, waypoints).ifPresent(directionsResponse -> {
            if (directionsResponse.getRoute() != null && directionsResponse.getRoute().getTrafast() != null && !directionsResponse.getRoute().getTrafast().isEmpty()) {
                NaverDirectionsResponse.Summary summary = directionsResponse.getRoute().getTrafast().get(0).getSummary();
                if (summary != null) {
                    double distanceInKm = summary.getDistance() / 1000.0;
                    response.setDistanceToDestination(String.format("%.1f", distanceInKm));
                    long durationSeconds = summary.getDuration() / 1000;
                    response.setEstimatedArrivalTime(shipment.getPickupDesiredAt().plusSeconds(durationSeconds));
                    log.info("전체 경로 ETA 계산 완료: 거리 {} km, 소요시간 {}초, 기준 시간 {}", String.format("%.1f", distanceInKm), durationSeconds, shipment.getPickupDesiredAt());
                }
            }
        });
    }

    /**
     * 차주 현재 위치를 기준으로 남은 경로 ETA/거리 계산.
     */
    private void calculateRemainingEtaAndDistance(Shipment shipment, ShipmentDetailResponse response) {
        if (shipment.getCurrentLocationPoint() == null || shipment.getDropoffPoint() == null) {
            log.warn("운송건 ID {}: 필수 좌표(현재위치 또는 목적지)가 누락되어 ETA를 계산할 수 없습니다.", shipment.getShipmentId());
            response.setDistanceToDestination(null);
            return;
        }

        String start = shipment.getCurrentLocationPoint().getX() + "," + shipment.getCurrentLocationPoint().getY();
        String goal = shipment.getDropoffPoint().getX() + "," + shipment.getDropoffPoint().getY();

        List<String> waypoints = new ArrayList<>();
        if (shipment.getWaypoint1Point() != null) {
            waypoints.add(shipment.getWaypoint1Point().getX() + "," + shipment.getWaypoint1Point().getY());
        }
        if (shipment.getWaypoint2Point() != null) {
            waypoints.add(shipment.getWaypoint2Point().getX() + "," + shipment.getWaypoint2Point().getY());
        }

        log.info("Naver Directions API 호출 (남은 경로) - 출발지: {}, 목적지: {}, 경유지: {}", start, goal, waypoints);

        Optional<NaverDirectionsResponse> directionsResponseOptional = naverDirectionsApiClient.getDirections(start, goal, waypoints);

        directionsResponseOptional.ifPresentOrElse(
                directionsResponse -> {
                    if (directionsResponse.getRoute() != null && directionsResponse.getRoute().getTrafast() != null && !directionsResponse.getRoute().getTrafast().isEmpty()) {
                        NaverDirectionsResponse.Summary summary = directionsResponse.getRoute().getTrafast().get(0).getSummary();
                        if (summary != null) {
                            response.setDistanceToDestination(String.format("%.1f", summary.getDistance() / 1000.0));
                            long durationSeconds = summary.getDuration() / 1000;
                            response.setEstimatedArrivalTime(LocalDateTime.now().plusSeconds(durationSeconds));
                            log.info("남은 경로 ETA 계산 완료: 거리 {}, 소요시간 {}초", response.getDistanceToDestination(), durationSeconds);
                        } else {
                            log.warn("Naver Directions API (남은 경로) Summary 정보가 없습니다. (Shipment ID: {})", shipment.getShipmentId());
                            response.setDistanceToDestination(null);
                        }
                    } else {
                        log.warn("Naver Directions API (남은 경로) 경로 정보가 없습니다. (Shipment ID: {})", shipment.getShipmentId());
                        response.setDistanceToDestination(null);
                    }
                },
                () -> {
                    log.error("Naver Directions API (남은 경로) 호출 실패 또는 응답 없음. (Shipment ID: {})", shipment.getShipmentId());
                    response.setDistanceToDestination(null);
                }
        );
    }
}
