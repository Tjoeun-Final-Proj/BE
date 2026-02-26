package com.tjoeun.boxmon.feature.shipment.mapper;

import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ShipmentCreateMapper {

    @Mapping(target = "shipmentId", ignore = true)
    @Mapping(target = "acceptedAt", ignore = true)
    @Mapping(target = "pickupAt", ignore = true)
    @Mapping(target = "dropoffAt", ignore = true)
    @Mapping(target = "waypoint1At", ignore = true)
    @Mapping(target = "waypoint2At", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "shipperCancelToggle", ignore = true)
    @Mapping(target = "driverCancelToggle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "currentLocationPoint", ignore = true)
    @Mapping(target = "dropoffPhotoUrl", ignore = true)
    @Mapping(target = "shipper", source = "shipper")
    @Mapping(target = "pickupPoint", source = "pickupPoint")
    @Mapping(target = "dropoffPoint", source = "dropoffPoint")
    @Mapping(target = "waypoint1Point", source = "waypoint1Point")
    @Mapping(target = "waypoint2Point", source = "waypoint2Point")
    @Mapping(target = "pickupAddress", source = "request.pickupAddress")
    @Mapping(target = "pickupDesiredAt", source = "request.pickupDesiredAt")
    @Mapping(target = "dropoffAddress", source = "request.dropoffAddress")
    @Mapping(target = "dropoffDesiredAt", source = "request.dropoffDesiredAt")
    @Mapping(target = "waypoint1Address", source = "request.waypoint1Address")
    @Mapping(target = "waypoint2Address", source = "request.waypoint2Address")
    @Mapping(target = "estimatedDistance", source = "estimatedDistance")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "platformFee", source = "platformFee")
    @Mapping(target = "profit", source = "profit")
    @Mapping(target = "cargoType", source = "request.cargoType")
    @Mapping(target = "cargoWeight", source = "request.cargoWeight")
    @Mapping(target = "cargoVolume", source = "request.cargoVolume")
    @Mapping(target = "vehicleType", source = "request.vehicleType")
    @Mapping(target = "needRefrigerate", source = "request.needRefrigerate")
    @Mapping(target = "needFreeze", source = "request.needFreeze")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "cargoPhotoUrl", source = "cargoPhotoUrl")
    @Mapping(target = "companyName", source = "normalizedCompanyName")
    @Mapping(target = "shipmentStatus", constant = "REQUESTED")
    @Mapping(target = "settlementStatus", constant = "INELIGIBLE")
    Shipment toShipment(
            Shipper shipper,
            ShipmentCreateRequest request,
            Point pickupPoint,
            Point dropoffPoint,
            Point waypoint1Point,
            Point waypoint2Point,
            Double estimatedDistance,
            BigDecimal price,
            BigDecimal platformFee,
            BigDecimal profit,
            String cargoPhotoUrl,
            String normalizedCompanyName
    );
}
