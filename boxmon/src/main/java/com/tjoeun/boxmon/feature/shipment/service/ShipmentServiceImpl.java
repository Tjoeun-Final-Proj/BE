package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Coordinate; // 추가
import org.locationtech.jts.geom.GeometryFactory; // 추가
import org.locationtech.jts.geom.Point; // 추가 (엔티티의 타입과 일치)
import org.locationtech.jts.geom.PrecisionModel; // 추가

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipperRepository shipperRepository;

    // GPS 표준 좌표계(WGS84)인 SRID 4326을 사용하는 Factory 생성
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public void createShipment(Long shipperId, ShipmentCreateRequest request) {
        // 1. 화주 조회
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new UserNotFoundException("화주를 찾을 수 없습니다."));

        // 2. 좌표 변환 (DTO의 Point -> JTS Point)
        Point pickupPoint = convertToJtsPoint(request.getPickupPoint());
        Point dropoffPoint = convertToJtsPoint(request.getDropoffPoint());
        Point waypoint1Point = convertToJtsPoint(request.getWaypoint1Point());
        Point waypoint2Point = convertToJtsPoint(request.getWaypoint2Point());

        // 3. 비용 및 상태 설정
        ShipmentStatus shipmentStatus = ShipmentStatus.REQUESTED;
        BigDecimal price = BigDecimal.valueOf(request.getPrice());
        BigDecimal platformFee = price.multiply(BigDecimal.valueOf(0.1)); ///TODO: 수수료율 하드 코딩 -> 추후 system_setting 테이블 생성 후 여기서 수수료율 전역으로 관리 예정
        BigDecimal profit = price.subtract(platformFee);

        // 4. Shipment 엔티티 생성
        Shipment shipment = Shipment.builder()
                .shipper(shipper)
                .pickupPoint(pickupPoint) // 변환된 JTS Point 사용
                .pickupAddress(request.getPickupAddress())
                .pickupDesiredAt(request.getPickupDesiredAt())
                .dropoffPoint(dropoffPoint) // 변환된 JTS Point 사용
                .dropoffAddress(request.getDropoffAddress())
                .dropoffDesiredAt(request.getDropoffDesiredAt())
                .waypoint1Point(waypoint1Point)
                .waypoint1Address(request.getWaypoint1Address())
                .waypoint2Point(waypoint2Point)
                .waypoint2Address(request.getWaypoint2Address())
                .price(price)
                .platformFee(platformFee)
                .profit(profit)
                .cargoType(request.getCargoType())
                .cargoWeight(request.getCargoWeight())
                .cargoVolume(request.getCargoVolume())
                .needRefrigerate(request.getNeedRefrigerate())
                .needFreeze(request.getNeedFreeze())
                .description(request.getDescription())
                .cargoPhotoUrl(request.getCargoPhotoUrl())
                .shipmentStatus(shipmentStatus)
                .build();

        // 5. 저장
        shipmentRepository.save(shipment);
    }

    /**
     * org.springframework.data.geo.Point를 org.locationtech.jts.geom.Point로 변환
     */
    // DTO에 담긴 x, y 좌표를 자동으로 JTS 전용 바이너리 객체로 변환해주는 메소드.
    private Point convertToJtsPoint(org.springframework.data.geo.Point source) {
        if (source == null) return null;
        // JTS는 기본적으로 x=경도(longitude), y=위도(latitude) 순서를 따릅니다.
        return geometryFactory.createPoint(new Coordinate(source.getX(), source.getY()));
    }
}
