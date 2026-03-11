package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.DriverInventoryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.DriverTodaySummaryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.MyUnassignedShipmentResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentPriceGuideRequest;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentPriceGuideResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperInventoryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperRecentShipmentResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperTodaySummaryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.UnassignedShipmentResponse;
import com.tjoeun.boxmon.feature.shipment.mapper.ShipmentMapper;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.global.naver.api.NaverDirectionsApiClient;
import com.tjoeun.boxmon.global.naver.dto.NaverDirectionsResponse;
import com.tjoeun.boxmon.global.util.AddressProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * Shipment 조회(Query) 계열을 담당하는 서비스.
 * 상세 조회, 미배차 목록 조회, ETA/거리 계산을 처리합니다.
 */
public class ShipmentQueryService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final double SHORT_DISTANCE_MAX_KM = 20.0;
    private static final double MID_DISTANCE_MAX_KM = 50.0;
    private static final double LONG_DISTANCE_MAX_KM = 100.0;
    private static final int SHORT_BASE_PRICE = 45000;
    private static final int MID_BASE_PRICE = 45000;
    private static final int LONG_BASE_PRICE = 81000;
    private static final int EXTRA_LONG_BASE_PRICE = 131000;
    private static final int MID_PRICE_PER_KM = 1200;
    private static final int LONG_PRICE_PER_KM = 1000;
    private static final int EXTRA_LONG_PRICE_PER_KM = 850;

    private final ShipmentRepository shipmentRepository;
    private final NaverDirectionsApiClient naverDirectionsApiClient;
    private final ShipmentDomainSupport support;
    private final ShipmentMapper shipmentMapper;

    public ShipmentPriceGuideResponse getShipmentPriceGuide(Long shipperId, ShipmentPriceGuideRequest request) {
        support.validateShipperAccess(shipperId);

        Point pickupPoint = support.convertToJtsPoint(request.getPickupPoint());
        Point dropoffPoint = support.convertToJtsPoint(request.getDropoffPoint());
        Point waypoint1Point = support.convertToJtsPoint(request.getWaypoint1Point());
        Point waypoint2Point = support.convertToJtsPoint(request.getWaypoint2Point());

        Double distanceKm = calculateDistance(pickupPoint, dropoffPoint, waypoint1Point, waypoint2Point)
                .orElseThrow(() -> new ExternalServiceException("운임 가이드 거리 계산에 실패했습니다."));

        return ShipmentPriceGuideResponse.builder()
                .estimatedDistanceKm(roundDistance(distanceKm))
                .recommendedPrice(calculateRecommendedPrice(distanceKm))
                .build();
    }

    /**
     * 배차 수락 화면용 상세 조회 (사진 URL 포함).
     */
    public ShipmentDetailResponse getShipmentAcceptDetail(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

        ShipmentDetailResponse response = shipmentMapper.toDetailResponse(shipment, true, true);
        calculateTotalEtaAndDistance(shipment, response);
        return response;
    }

    /**
     * 상세 조회 시 차주 위치 유무에 따라 ETA 계산 분기를 처리합니다.
     * - 차주 현재 위치 존재: 남은 경로 ETA
     * - 그 외: 전체 경로 ETA
     */
    public ShipmentDetailResponse getShipmentDetail(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

        ShipmentDetailResponse response = shipmentMapper.toDetailResponse(shipment, true, true);

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
     * 차주 홈용 오늘 요약 정보를 조회합니다.
     */
    public DriverTodaySummaryResponse getMyDriverTodaySummary(Long driverId) {
        support.validateDriverAccess(driverId);

        LocalDate today = LocalDate.now(KST);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);
        List<ShipmentStatus> scheduleStatuses = List.of(ShipmentStatus.ASSIGNED, ShipmentStatus.IN_TRANSIT);

        int todayScheduleCount = Math.toIntExact(
                shipmentRepository.countByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusIn(
                        driverId, startOfDay, endOfDay, scheduleStatuses
                )
        );

        LocalDateTime firstPickupDesiredAt = shipmentRepository
                .findFirstByDriver_DriverIdAndPickupDesiredAtBetweenAndShipmentStatusInOrderByPickupDesiredAtAsc(
                        driverId, startOfDay, endOfDay, scheduleStatuses
                )
                .map(Shipment::getPickupDesiredAt)
                .orElse(null);

        int inTransitCount = Math.toIntExact(
                shipmentRepository.countByDriver_DriverIdAndShipmentStatus(driverId, ShipmentStatus.IN_TRANSIT)
        );

        return DriverTodaySummaryResponse.builder()
                .todayScheduleCount(todayScheduleCount)
                .firstPickupDesiredAt(firstPickupDesiredAt)
                .inTransitCount(inTransitCount)
                .build();
    }

    /**
     * 화주 홈용 요약 정보를 조회합니다.
     * 전체 누적 기준으로 상태별 건수를 반환합니다.
     */
    public ShipperTodaySummaryResponse getMyShipperTodaySummary(Long shipperId) {
        support.validateShipperAccess(shipperId);

        int requestedCount = Math.toIntExact(
                shipmentRepository.countByShipper_ShipperIdAndShipmentStatus(shipperId, ShipmentStatus.REQUESTED)
        );
        int assignedCount = Math.toIntExact(
                shipmentRepository.countByShipper_ShipperIdAndShipmentStatus(shipperId, ShipmentStatus.ASSIGNED)
        );
        int inTransitCount = Math.toIntExact(
                shipmentRepository.countByShipper_ShipperIdAndShipmentStatus(shipperId, ShipmentStatus.IN_TRANSIT)
        );
        int doneCount = Math.toIntExact(
                shipmentRepository.countByShipper_ShipperIdAndShipmentStatus(shipperId, ShipmentStatus.DONE)
        );

        return ShipperTodaySummaryResponse.builder()
                .requestedCount(requestedCount)
                .assignedCount(assignedCount)
                .inTransitCount(inTransitCount)
                .doneCount(doneCount)
                .build();
    }

    /**
     * 화주 홈용 최근 운송 1건 정보를 조회합니다.
     */
    public ShipperRecentShipmentResponse getMyRecentShipperShipment(Long shipperId) {
        support.validateShipperAccess(shipperId);

        Optional<Shipment> recentShipment = shipmentRepository.findFirstByShipper_ShipperIdOrderByCreatedAtDesc(shipperId);
        if (recentShipment.isEmpty()) {
            return null;
        }

        Shipment shipment = recentShipment.get();
        LocalDateTime lastUpdatedAt = resolveLastUpdatedAt(shipment);

        return ShipperRecentShipmentResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .routeText(buildRouteText(shipment))
                .shipmentStatus(shipment.getShipmentStatus() == null ? null : shipment.getShipmentStatus().getDescription())
                .lastUpdatedAt(lastUpdatedAt)
                .lastUpdatedLabel(toLastUpdatedLabel(lastUpdatedAt))
                .build();
    }

    private LocalDateTime resolveLastUpdatedAt(Shipment shipment) {
        return Stream.of(
                        shipment.getCreatedAt(),
                        shipment.getAcceptedAt(),
                        shipment.getPickupAt(),
                        shipment.getDropoffAt()
                )
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(shipment.getCreatedAt());
    }

    private String buildRouteText(Shipment shipment) {
        String pickup = AddressProcessor.simplifiy(shipment.getPickupAddress());
        String dropoff = AddressProcessor.simplifiy(shipment.getDropoffAddress());

        String pickupText = (pickup == null || pickup.isBlank()) ? shipment.getPickupAddress() : pickup;
        String dropoffText = (dropoff == null || dropoff.isBlank()) ? shipment.getDropoffAddress() : dropoff;

        String safePickupText = (pickupText == null || pickupText.isBlank()) ? "출발지" : pickupText;
        String safeDropoffText = (dropoffText == null || dropoffText.isBlank()) ? "도착지" : dropoffText;
        return safePickupText + " → " + safeDropoffText;
    }

    private String toLastUpdatedLabel(LocalDateTime lastUpdatedAt) {
        if (lastUpdatedAt == null) {
            return null;
        }

        LocalDate today = LocalDate.now(KST);
        LocalDate updatedDate = lastUpdatedAt.toLocalDate();
        if (updatedDate.equals(today)) {
            long minutesAgo = ChronoUnit.MINUTES.between(lastUpdatedAt, LocalDateTime.now(KST));
            if (minutesAgo < 1) {
                minutesAgo = 1;
            }
            return minutesAgo + "분 전 업데이트";
        }
        if (updatedDate.equals(today.minusDays(1))) {
            return "어제 업데이트";
        }
        return lastUpdatedAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " 업데이트";
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

    private Optional<Double> calculateDistance(Point startPoint, Point goalPoint, Point waypoint1Point, Point waypoint2Point) {
        if (startPoint == null || goalPoint == null) {
            throw new IllegalArgumentException("출발지와 도착지 좌표는 필수입니다.");
        }

        String start = startPoint.getX() + "," + startPoint.getY();
        String goal = goalPoint.getX() + "," + goalPoint.getY();
        List<String> waypoints = new ArrayList<>();
        if (waypoint1Point != null) {
            waypoints.add(waypoint1Point.getX() + "," + waypoint1Point.getY());
        }
        if (waypoint2Point != null) {
            waypoints.add(waypoint2Point.getX() + "," + waypoint2Point.getY());
        }

        return naverDirectionsApiClient.getDirections(start, goal, waypoints)
                .map(this::extractDistanceKm);
    }

    private Double extractDistanceKm(NaverDirectionsResponse directionsResponse) {
        if (directionsResponse.getRoute() == null
                || directionsResponse.getRoute().getTrafast() == null
                || directionsResponse.getRoute().getTrafast().isEmpty()
                || directionsResponse.getRoute().getTrafast().get(0).getSummary() == null) {
            throw new ExternalServiceException("길찾기 응답에 경로 정보가 없습니다.");
        }
        return directionsResponse.getRoute().getTrafast().get(0).getSummary().getDistance() / 1000.0;
    }

    private Double roundDistance(Double distanceKm) {
        return BigDecimal.valueOf(distanceKm)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Integer calculateRecommendedPrice(Double distanceKm) {
        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        BigDecimal recommendedPrice;

        if (distanceKm < SHORT_DISTANCE_MAX_KM) {
            recommendedPrice = BigDecimal.valueOf(SHORT_BASE_PRICE);
        } else if (distanceKm < MID_DISTANCE_MAX_KM) {
            recommendedPrice = BigDecimal.valueOf(MID_BASE_PRICE)
                    .add(distance.multiply(BigDecimal.valueOf(MID_PRICE_PER_KM)));
        } else if (distanceKm < LONG_DISTANCE_MAX_KM) {
            recommendedPrice = BigDecimal.valueOf(LONG_BASE_PRICE)
                    .add(distance.multiply(BigDecimal.valueOf(LONG_PRICE_PER_KM)));
        } else {
            recommendedPrice = BigDecimal.valueOf(EXTRA_LONG_BASE_PRICE)
                    .add(distance.multiply(BigDecimal.valueOf(EXTRA_LONG_PRICE_PER_KM)));
        }

        return recommendedPrice.setScale(0, RoundingMode.HALF_UP).intValueExact();
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
