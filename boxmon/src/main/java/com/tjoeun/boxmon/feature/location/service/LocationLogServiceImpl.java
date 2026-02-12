package com.tjoeun.boxmon.feature.location.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationLogServiceImpl implements LocationLogService {

    private final LocationLogRepository locationLogRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipperRepository shipperRepository;
    private final DriverRepository driverRepository;

    @Override
    public void saveLocationLog(Long driverId, LocationLogRequest request) {
        // 1. 운송건 조회
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new UserNotFoundException("운송건을 찾을 수 없습니다.")); // TODO: ShipmentNotFoundException 정의

        // 2. 차주 조회
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new UserNotFoundException("차주를 찾을 수 없습니다."));

        // 3. 화주 조회 (LocationLog의 비정규화된 shipper 필드를 위함)
        // Shipment 엔티티의 shipper_id는 nullable=false 이므로, shipment.getShipper()는 null이 될 수 없습니다.
        // 하지만 shipperRepository를 통해 다시 조회하여 항상 관리(managed) 상태의 Shipper 엔티티를 사용하고
        // 최신 상태를 보장합니다.
        Shipper shipper = shipperRepository.findById(shipment.getShipper().getShipperId())
                .orElseThrow(() -> new UserNotFoundException("운송건에 연결된 화주를 찾을 수 없습니다."));

        // 4. LocationLog 엔티티 생성
        LocationLog locationLog = LocationLog.builder()
                .shipment(shipment)
                .shipper(shipper)
                .driver(driver)
                .locationData(request.getLocationChunk())
                .build();

        // 5. 저장
        locationLogRepository.save(locationLog);
    }
}
