package com.tjoeun.boxmon.feature.location.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 추가 필요
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.location.domain.LocationLog;
import com.tjoeun.boxmon.feature.location.dto.LocationLogRequest;
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
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // 1. @RequiredArgsConstructor를 제거하고 생성자를 직접 작성합니다.
    public LocationLogServiceImpl(
            LocationLogRepository locationLogRepository,
            ShipmentRepository shipmentRepository,
            ShipperRepository shipperRepository,
            DriverRepository driverRepository
    ) {
        this.locationLogRepository = locationLogRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipperRepository = shipperRepository;
        this.driverRepository = driverRepository;

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
}