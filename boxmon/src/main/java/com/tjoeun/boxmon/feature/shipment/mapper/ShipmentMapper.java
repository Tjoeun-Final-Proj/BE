package com.tjoeun.boxmon.feature.shipment.mapper;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.CargoType;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.DriverSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.UnassignedShipmentResponse;
import com.tjoeun.boxmon.feature.user.domain.VehicleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(target = "shipmentNumber", ignore = true)
    @Mapping(target = "distanceToDestination", ignore = true)
    @Mapping(target = "estimatedArrivalTime", ignore = true)
    @Mapping(target = "shipperId", source = "shipper.shipperId")
    @Mapping(target = "shipperName", source = "shipper.user.name")
    @Mapping(target = "driverId", expression = "java(shipment.getDriver() != null ? shipment.getDriver().getDriverId() : null)")
    @Mapping(target = "driverName", expression = "java(shipment.getDriver() != null ? shipment.getDriver().getUser().getName() : \"미배차\")")
    @Mapping(target = "currentDriverPoint", source = "currentLocationPoint", qualifiedByName = "toSpringPoint")
    @Mapping(target = "cargoType", source = "cargoType", qualifiedByName = "toCargoDescription")
    @Mapping(target = "vehicleType", source = "vehicleType", qualifiedByName = "toVehicleDescription")
    @Mapping(target = "price", source = "price", qualifiedByName = "roundMoney")
    @Mapping(target = "platformFee", source = "platformFee", qualifiedByName = "roundMoney")
    @Mapping(target = "profit", source = "profit", qualifiedByName = "roundMoney")
    @Mapping(target = "pickupPoint", source = "pickupPoint", qualifiedByName = "toSpringPoint")
    @Mapping(target = "dropoffPoint", source = "dropoffPoint", qualifiedByName = "toSpringPoint")
    @Mapping(target = "companyName", source = "companyName", qualifiedByName = "normalizeCompanyName")
    ShipmentDetailResponse toDetailResponseBase(Shipment shipment);

    default ShipmentDetailResponse toDetailResponse(
            Shipment shipment,
            boolean includeCargoPhotoUrl,
            boolean includeDropoffPhotoUrl
    ) {
        ShipmentDetailResponse response = toDetailResponseBase(shipment);
        response.setShipmentNumber(buildShipmentNumber(shipment));

        if (!includeCargoPhotoUrl) {
            response.setCargoPhotoUrl(null);
        }
        if (!includeDropoffPhotoUrl) {
            response.setDropoffPhotoUrl(null);
        }

        return response;
    }

    @Mapping(target = "price", source = "price", qualifiedByName = "roundMoney")
    @Mapping(target = "shipmentStatus", source = "shipmentStatus", qualifiedByName = "toShipmentStatusDescription")
    ShipperSettlementListResponse toShipperSettlementListResponse(Shipment shipment);

    @Mapping(target = "shipmentStatus", source = "shipmentStatus", qualifiedByName = "toShipmentStatusDescription")
    @Mapping(target = "profit", source = "profit", qualifiedByName = "roundMoney")
    DriverSettlementListResponse toDriverSettlementListResponse(Shipment shipment);

    @Mapping(target = "vehicleType", source = "vehicleType", qualifiedByName = "toVehicleDescription")
    @Mapping(target = "profit", source = "profit", qualifiedByName = "roundMoney")
    UnassignedShipmentResponse toUnassignedShipmentResponse(Shipment shipment);

    @Named("toSpringPoint")
    default org.springframework.data.geo.Point toSpringPoint(Point jtsPoint) {
        if (jtsPoint == null) {
            return null;
        }
        return new org.springframework.data.geo.Point(jtsPoint.getX(), jtsPoint.getY());
    }

    @Named("toVehicleDescription")
    default String toVehicleDescription(VehicleType vehicleType) {
        return vehicleType == null ? null : vehicleType.getDescription();
    }

    @Named("toCargoDescription")
    default String toCargoDescription(CargoType cargoType) {
        return cargoType == null ? null : cargoType.getDescription();
    }

    @Named("toShipmentStatusDescription")
    default String toShipmentStatusDescription(ShipmentStatus shipmentStatus) {
        return shipmentStatus == null ? null : shipmentStatus.getDescription();
    }

    @Named("roundMoney")
    default BigDecimal roundMoney(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    @Named("normalizeCompanyName")
    default String normalizeCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return "개인화주";
        }
        return companyName.trim();
    }

    default String buildShipmentNumber(Shipment shipment) {
        return String.format("%s-%s-%03d",
                shipment.getCargoType().getCode(),
                shipment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyMMdd")),
                shipment.getShipmentId() % 1000);
    }
}
