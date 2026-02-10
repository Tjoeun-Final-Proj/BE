package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipperRepository shipperRepository;

    @Override
    public void createShipment(Long shipperId, ShipmentCreateRequest request) {
        // 1. 화주 조회
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new UserNotFoundException("화주를 찾을 수 없습니다."));

        // 2. Shipment 엔티티 생성 및 매핑
        // ShipmentStatus는 Enum으로 변경하는 것이 좋지만, 일단 String으로 사용
        // TODO: ShipmentStatus를 Enum으로 변경 고려
        String shipmentStatus = "REQUESTED"; // 초기 상태

        // platformFee와 profit 계산
        // TODO: 수수료 정책에 따라 정확한 계산 로직 구현 필요
        Integer price = request.getPrice();
        Integer platformFee = (int) (price * 0.1); // 10% 수수료
        Integer profit = price - platformFee;

        Shipment shipment = Shipment.builder()
                .shipper(shipper)
                .pickupPoint(request.getPickupPoint())
                .pickupAddress(request.getPickupAddress())
                .pickupDesiredAt(request.getPickupDesiredAt())
                .dropoffPoint(request.getDropoffPoint())
                .dropoffAddress(request.getDropoffAddress())
                .dropoffDesiredAt(request.getDropoffDesiredAt())
                .waypoint1Point(request.getWaypoint1Point())
                .waypoint1Address(request.getWaypoint1Address())
                .waypoint2Point(request.getWaypoint2Point())
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
                // shipperCancelToggle, driverCancelToggle은 @Builder.Default로 설정됨
                .build();

        // 3. Shipment 저장
        shipmentRepository.save(shipment);
    }
}
