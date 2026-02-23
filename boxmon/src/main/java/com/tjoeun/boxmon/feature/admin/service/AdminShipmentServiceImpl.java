package com.tjoeun.boxmon.feature.admin.service;

import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.feature.admin.dto.AdminAssignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminAssignedShipmentDetailResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentDetailResponse;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
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

    @Override
    public List<AdminAssignedShipmentBasicResponse> getAssignedBasic(Long adminId) {
        validateAdminAccess(adminId);

        List<Shipment> shipments = shipmentRepository.findByShipmentStatus(ShipmentStatus.ASSIGNED);
        return shipments.stream()
                .map(this::toAssignedBasicResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AdminAssignedShipmentDetailResponse getAssignedDetail(Long adminId, Long shipmentId) {
        validateAdminAccess(adminId);

        Shipment shipment = shipmentRepository.findByShipmentIdAndShipmentStatus(shipmentId, ShipmentStatus.ASSIGNED)
                .orElseThrow(() -> new ShipmentNotFoundException("배차된 화물을 찾을 수 없습니다."));

        return toAssignedDetailResponse(shipment);
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

    private AdminAssignedShipmentBasicResponse toAssignedBasicResponse(Shipment shipment) {
        return AdminAssignedShipmentBasicResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .driverName(shipment.getDriver() != null ? shipment.getDriver().getUser().getName() : null)
                .pickupAddress(shipment.getPickupAddress())
                .dropoffAddress(shipment.getDropoffAddress())
                .shipmentStatus(shipment.getShipmentStatus())
                .build();
    }

    private AdminAssignedShipmentDetailResponse toAssignedDetailResponse(Shipment shipment) {
        return AdminAssignedShipmentDetailResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .shipperId(shipment.getShipper() != null ? shipment.getShipper().getShipperId() : null)
                .driverId(shipment.getDriver() != null ? shipment.getDriver().getDriverId() : null)
                .acceptedAt(shipment.getAcceptedAt())
                .pickupAddress(shipment.getPickupAddress())
                .pickupDesiredAt(shipment.getPickupDesiredAt())
                .pickupAt(shipment.getPickupAt())
                .dropoffAddress(shipment.getDropoffAddress())
                .dropoffDesiredAt(shipment.getDropoffDesiredAt())
                .dropoffAt(shipment.getDropoffAt())
                .waypoint1Address(shipment.getWaypoint1Address())
                .waypoint1At(shipment.getWaypoint1At())
                .waypoint2Address(shipment.getWaypoint2Address())
                .waypoint2At(shipment.getWaypoint2At())
                .estimatedDistance(shipment.getEstimatedDistance())
                .price(shipment.getPrice())
                .platformFee(shipment.getPlatformFee())
                .profit(shipment.getProfit())
                .shipmentStatus(shipment.getShipmentStatus())
                .cargoType(shipment.getCargoType())
                .cargoWeight(shipment.getCargoWeight())
                .cargoVolume(shipment.getCargoVolume())
                .vehicleType(shipment.getVehicleType())
                .needRefrigerate(shipment.getNeedRefrigerate())
                .needFreeze(shipment.getNeedFreeze())
                .description(shipment.getDescription())
                .cargoPhotoUrl(shipment.getCargoPhotoUrl())
                .dropoffPhotoUrl(shipment.getDropoffPhotoUrl())
                .shipperCancelToggle(shipment.getShipperCancelToggle())
                .driverCancelToggle(shipment.getDriverCancelToggle())
                .pickupPoint(convertToSpringPoint(shipment.getPickupPoint()))
                .dropoffPoint(convertToSpringPoint(shipment.getDropoffPoint()))
                .waypoint1Point(convertToSpringPoint(shipment.getWaypoint1Point()))
                .waypoint2Point(convertToSpringPoint(shipment.getWaypoint2Point()))
                .createdAt(shipment.getCreatedAt())
                .currentLocationPoint(convertToSpringPoint(shipment.getCurrentLocationPoint()))
                .settlementStatus(shipment.getSettlementStatus())
                .build();
    }

    private org.springframework.data.geo.Point convertToSpringPoint(Point jtsPoint) {
        if (jtsPoint == null) return null;
        return new org.springframework.data.geo.Point(jtsPoint.getX(), jtsPoint.getY());
    }
}
