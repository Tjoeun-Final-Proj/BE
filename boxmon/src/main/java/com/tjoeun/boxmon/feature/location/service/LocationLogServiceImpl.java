package com.tjoeun.boxmon.feature.location.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 추가 필요
import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.location.domain.LocationLog;
import com.tjoeun.boxmon.feature.location.dto.LocationLogRequest;
import com.tjoeun.boxmon.feature.location.dto.LocationRoutePointResponse;
import com.tjoeun.boxmon.feature.location.dto.LocationRouteResponse;
import com.tjoeun.boxmon.feature.location.repository.LocationLogRepository;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.repository.DriverRepository;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class LocationLogServiceImpl implements LocationLogService {

    private final LocationLogRepository locationLogRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipperRepository shipperRepository;
    private final DriverRepository driverRepository;
    private final AdminRepository adminRepository;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private static final int DEFAULT_MAX_POINTS = 5000;
    private static final int MAX_ALLOWED_POINTS = 10000;

    // 1. @RequiredArgsConstructor를 제거하고 생성자를 직접 작성합니다.
    public LocationLogServiceImpl(
            LocationLogRepository locationLogRepository,
            ShipmentRepository shipmentRepository,
            ShipperRepository shipperRepository,
            DriverRepository driverRepository,
            AdminRepository adminRepository
    ) {
        this.locationLogRepository = locationLogRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipperRepository = shipperRepository;
        this.driverRepository = driverRepository;
        this.adminRepository = adminRepository;

        // 2. ObjectMapper를 직접 생성하여 할당합니다. 빈 등록 에러를 피할 수 있습니다.
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()); // 날짜 데이터 처리를 위한 모듈
    }

    @Override
    public void saveLocationLog(Long driverId, LocationLogRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new UserNotFoundException("운송건을 찾을 수 없습니다."));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new UserNotFoundException("차주를 찾을 수 없습니다."));

        Shipper shipper = shipperRepository.findById(shipment.getShipper().getShipperId())
                .orElseThrow(() -> new UserNotFoundException("운송건에 연결된 화주를 찾을 수 없습니다."));

        updateShipmentCurrentLocation(shipment, request.getLocationChunk());

        LocationLog locationLog = LocationLog.builder()
                .shipment(shipment)
                .shipper(shipper)
                .driver(driver)
                .locationData(request.getLocationChunk())
                .build();

        locationLogRepository.save(locationLog);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationRouteResponse getRoute(Long requesterId, Long shipmentId, LocalDateTime from, LocalDateTime to, Integer maxPoints) {
        validateRange(from, to);
        int resolvedMaxPoints = resolveMaxPoints(maxPoints);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

        validateRouteAccess(requesterId, shipment);

        List<LocationLog> logs = loadLocationLogs(shipmentId, from, to);
        List<LocationRoutePointResponse> points = new ArrayList<>();
        boolean truncated = false;

        for (LocationLog logEntry : logs) {
            if (points.size() >= resolvedMaxPoints) {
                truncated = true;
                break;
            }

            List<LocationRoutePointResponse> parsedPoints = parseChunkPoints(logEntry.getLocationData(), shipmentId, logEntry.getLocationId());
            for (LocationRoutePointResponse point : parsedPoints) {
                if (points.size() >= resolvedMaxPoints) {
                    truncated = true;
                    break;
                }
                points.add(point);
            }
        }

        return LocationRouteResponse.builder()
                .shipmentId(shipmentId)
                .pointCount(points.size())
                .truncated(truncated)
                .points(points)
                .build();
    }

    private void updateShipmentCurrentLocation(Shipment shipment, String locationChunk) {
        try {
            List<Map<String, Object>> locations = objectMapper.readValue(
                    locationChunk,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            if (locations != null && !locations.isEmpty()) {
                Map<String, Object> lastLocation = locations.get(locations.size() - 1);

                // JSON에서 꺼낼 때 숫자 타입 변환 예외 방지를 위해 Number 사용 권장
                double lat = ((Number) lastLocation.get("lat")).doubleValue();
                double lng = ((Number) lastLocation.get("lng")).doubleValue();

                Point currentPoint = geometryFactory.createPoint(new Coordinate(lng, lat));

                shipment.setCurrentLocationPoint(currentPoint);
                log.info("운송건 ID {}: 차주 최신 위치 업데이트 완료 ({}, {})", shipment.getShipmentId(), lat, lng);
            }
        } catch (Exception e) {
            log.error("위치 데이터 파싱 중 오류 발생: {}", e.getMessage());
        }
    }

    private void validateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다.");
        }
    }

    private int resolveMaxPoints(Integer maxPoints) {
        if (maxPoints == null) {
            return DEFAULT_MAX_POINTS;
        }
        if (maxPoints < 1) {
            throw new IllegalArgumentException("maxPoints는 1 이상이어야 합니다.");
        }
        return Math.min(maxPoints, MAX_ALLOWED_POINTS);
    }

    private void validateRouteAccess(Long requesterId, Shipment shipment) {
        if (requesterId == null) {
            throw new RoleAccessDeniedException("인증 정보가 올바르지 않습니다.");
        }

        Long shipperId = shipment.getShipper() != null ? shipment.getShipper().getShipperId() : null;
        Long driverId = shipment.getDriver() != null ? shipment.getDriver().getDriverId() : null;

        boolean isShipper = shipperId != null && shipperId.equals(requesterId);
        boolean isDriver = driverId != null && driverId.equals(requesterId);
        boolean isAdmin = adminRepository.existsById(requesterId);

        if (!isShipper && !isDriver && !isAdmin) {
            throw new RoleAccessDeniedException("해당 운송건의 경로를 조회할 권한이 없습니다.");
        }
    }

    private List<LocationLog> loadLocationLogs(Long shipmentId, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return locationLogRepository.findByShipment_ShipmentIdAndCreatedAtBetweenOrderByCreatedAtAscLocationIdAsc(shipmentId, from, to);
        }
        return locationLogRepository.findByShipment_ShipmentIdOrderByCreatedAtAscLocationIdAsc(shipmentId);
    }

    private List<LocationRoutePointResponse> parseChunkPoints(String chunk, Long shipmentId, Long locationId) {
        try {
            List<Map<String, Object>> locations = objectMapper.readValue(chunk, new TypeReference<List<Map<String, Object>>>() {});
            List<LocationRoutePointResponse> points = new ArrayList<>();

            for (Map<String, Object> item : locations) {
                Double lat = toDouble(item.get("lat"));
                Double lng = toDouble(item.get("lng"));
                if (lat == null || lng == null) {
                    continue;
                }

                Object atValue = item.get("at");
                points.add(LocationRoutePointResponse.builder()
                        .lat(lat)
                        .lng(lng)
                        .at(atValue == null ? null : String.valueOf(atValue))
                        .build());
            }
            return points;
        } catch (Exception e) {
            log.warn("경로 청크 파싱 실패. shipmentId={}, locationId={}", shipmentId, locationId, e);
            return List.of();
        }
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
