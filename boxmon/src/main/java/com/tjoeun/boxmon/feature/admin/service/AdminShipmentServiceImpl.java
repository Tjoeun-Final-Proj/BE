package com.tjoeun.boxmon.feature.admin.service;

import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentDetailResponse;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminShipmentServiceImpl implements AdminShipmentService {

    private final AdminRepository adminRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    public List<AdminUnassignedShipmentBasicResponse> getUnassignedBasic(Long adminId) {
        validateAdminAccess(adminId);

        List<Shipment> shipments = shipmentRepository.findByShipmentStatusOrderByCreatedAtDesc(ShipmentStatus.REQUESTED);
        return shipments.stream()
                .map(this::toBasicResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AdminUnassignedShipmentDetailResponse getUnassignedDetail(Long adminId, Long shipmentId) {
        validateAdminAccess(adminId);

        Shipment shipment = shipmentRepository.findByShipmentIdAndShipmentStatus(shipmentId, ShipmentStatus.REQUESTED)
                .orElseThrow(() -> new ShipmentNotFoundException("미배차 화물을 찾을 수 없습니다."));

        return toDetailResponse(shipment);
    }

    private void validateAdminAccess(Long adminId) {
        if (!adminRepository.existsById(adminId)) {
            throw new RoleAccessDeniedException("Admin access required.");
        }
    }

    private AdminUnassignedShipmentBasicResponse toBasicResponse(Shipment shipment) {
        return AdminUnassignedShipmentBasicResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .shipperName(shipment.getShipper().getUser().getName())
                .pickupAddress(shipment.getPickupAddress())
                .dropoffAddress(shipment.getDropoffAddress())
                .build();
    }

    private AdminUnassignedShipmentDetailResponse toDetailResponse(Shipment shipment) {
        return AdminUnassignedShipmentDetailResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .shipperName(shipment.getShipper().getUser().getName())
                .pickupAddress(shipment.getPickupAddress())
                .dropoffAddress(shipment.getDropoffAddress())
                .pickupDesiredAt(shipment.getPickupDesiredAt())
                .dropoffDesiredAt(shipment.getDropoffDesiredAt())
                .waypoint1At(shipment.getWaypoint1At())
                .waypoint2At(shipment.getWaypoint2At())
                .price(roundMoney(shipment.getPrice()))
                .profit(roundMoney(shipment.getProfit()))
                .platformFee(roundMoney(shipment.getPlatformFee()))
                .cargoType(shipment.getCargoType())
                .cargoWeight(shipment.getCargoWeight())
                .cargoVolume(shipment.getCargoVolume())
                .needRefrigerate(shipment.getNeedRefrigerate())
                .needFreeze(shipment.getNeedFreeze())
                .description(shipment.getDescription())
                .cargoPhotoUrl(shipment.getCargoPhotoUrl())
                .createdAt(shipment.getCreatedAt())
                .vehicleType(shipment.getVehicleType().getDescription())
                .estimatedDistance(shipment.getEstimatedDistance())
                .build();
    }

    private BigDecimal roundMoney(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(0, RoundingMode.HALF_UP);
    }
}
