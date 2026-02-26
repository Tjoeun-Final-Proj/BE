package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.feature.user.repository.DriverRepository;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
/**
 * Shipment 도메인 공통 지원 컴포넌트.
 * 권한 검증, 연월 파라미터 검증, 좌표 변환, 회사명 정규화를 제공합니다.
 */
public class ShipmentDomainSupport {

    private final ShipperRepository shipperRepository;
    private final DriverRepository driverRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public ShipmentDomainSupport(ShipperRepository shipperRepository, DriverRepository driverRepository) {
        this.shipperRepository = shipperRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * 화주 권한 검증.
     */
    public void validateShipperAccess(Long shipperId) {
        if (!shipperRepository.existsById(shipperId)) {
            throw new RoleAccessDeniedException("Shipper access required.");
        }
    }

    /**
     * 차주 권한 검증.
     */
    public void validateDriverAccess(Long driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new RoleAccessDeniedException("Driver access required.");
        }
    }

    /**
     * 정산 목록 조회용 연/월 파라미터 검증.
     */
    public void validateYearMonth(int year, int month) {
        if (year < 1) {
            throw new IllegalArgumentException("year must be a positive integer.");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12.");
        }
    }

    /**
     * Spring Point -> JTS Point 변환 (DB 저장용).
     */
    public Point convertToJtsPoint(org.springframework.data.geo.Point source) {
        if (source == null) {
            return null;
        }
        return geometryFactory.createPoint(new Coordinate(source.getX(), source.getY()));
    }

    /**
     * 회사명 정규화: null/blank면 '개인화주' 기본값.
     */
    public String normalizeCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return "개인화주";
        }
        return companyName.trim();
    }

}
