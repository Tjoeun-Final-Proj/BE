package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentListResponse;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Coordinate; // 추가
import org.locationtech.jts.geom.GeometryFactory; // 추가
import org.locationtech.jts.geom.Point; // 추가 (엔티티의 타입과 일치)
import org.locationtech.jts.geom.PrecisionModel; // 추가
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    @Value("${google.maps.api-key}")
    private String googleMapsApiKey; // 필드에 바로 주입

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
     * 내 화물 목록 조회 (필터링 및 최신순 정렬)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ShipmentListResponse> getMyShipmentList(Long shipperId, ShipmentStatus status) {
        List<Shipment> shipments;

        // 상태값(status) 유무에 따라 레포지토리 메서드 분기 호출
        if (status != null) {
            shipments = shipmentRepository.findByShipper_ShipperIdAndShipmentStatusOrderByCreatedAtDesc(shipperId, status);
        } else {
            shipments = shipmentRepository.findByShipper_ShipperIdOrderByCreatedAtDesc(shipperId);
        }

        // 스트림을 사용하여 엔티티 리스트를 DTO 리스트로 변환
        return shipments.stream()
                .map(this::toShipmentListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Entity -> ListResponse DTO 변환 (내부 메서드)
     */
    private ShipmentListResponse toShipmentListResponse(Shipment shipment) {
        return ShipmentListResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .shipmentStatus(shipment.getShipmentStatus())
                .cargoType(shipment.getCargoType())
                .cargoWeight(shipment.getCargoWeight())
                .pickupAddress(shipment.getPickupAddress())
                .dropoffAddress(shipment.getDropoffAddress())
                .pickupDesiredAt(shipment.getPickupDesiredAt())
                .dropoffDesiredAt(shipment.getDropoffDesiredAt())
                .price(shipment.getPrice())
                .createdAt(shipment.getCreatedAt())
                // 배차 완료된 경우에만 기사님 성함 노출
                .driverName(shipment.getDriver() != null ? shipment.getDriver().getUser().getName() : "미배차")
                .build();
    }
    /**
     * 화물 상세 정보 조회 (추가 구현)
     */
    @Override
    @Transactional(readOnly = true)
    public ShipmentDetailResponse getShipmentDetail(Long shipmentId) {
        // 1. 엔티티 조회
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new UserNotFoundException("운송건을 찾을 수 없습니다."));

        // 2. 기본 정보 DTO 변환
        ShipmentDetailResponse response = toDetailResponse(shipment);

        // 3. 차주가 배차되었고 최신 위치 정보가 있는 경우에만 Google API 호출하여 ETA 계산
        if (shipment.getDriver() != null && shipment.getCurrentLocationPoint() != null) {
            calculateEtaAndDistance(shipment, response);
        } else {
            log.warn("차주 위치 정보가 없어 ETA를 계산하지 않습니다. (Shipment ID: {})", shipmentId);
        }

        return response;
    }

    /**
     * Google Directions API 연동 로직
     */
    private void calculateEtaAndDistance(Shipment shipment, ShipmentDetailResponse response) {
        // 1. 방어 로직: 필수 좌표가 없으면 중단 (NPE 방지)
        if (shipment.getCurrentLocationPoint() == null || shipment.getDropoffPoint() == null) {
            log.warn("운송건 ID {}: 필수 좌표(현재위치 또는 목적지)가 누락되어 ETA를 계산할 수 없습니다.", shipment.getShipmentId());
            response.setDistanceToDestination("좌표 누락");
            return;
        }

        // 2. 변수 선언 (Google API는 "위도,경도" 순서 문자열 필요)
        String origin = shipment.getCurrentLocationPoint().getY() + "," + shipment.getCurrentLocationPoint().getX();
        String destination = shipment.getDropoffPoint().getY() + "," + shipment.getDropoffPoint().getX();

        // 3. 로그 출력 (변수 선언 이후에 찍어야 합니다!)
        log.info("Google Directions API 호출 - 출발지: {}, 목적지: {}", origin, destination);

        try (GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleMapsApiKey)
                .build()) {

            DirectionsResult result = DirectionsApi.newRequest(context)
                    .mode(TravelMode.DRIVING)
                    .origin(origin)
                    .destination(destination)
                    .language("ko")
                    .await();

            if (result.routes != null && result.routes.length > 0 && result.routes[0].legs.length > 0) {
                var leg = result.routes[0].legs[0];

                // 남은 거리 설정
                response.setDistanceToDestination(leg.distance.humanReadable);

                // 예상 도착 시간 계산 (현재 서버 시간 + 소요 초)
                long durationSeconds = leg.duration.inSeconds;
                response.setEstimatedArrivalTime(LocalDateTime.now().plusSeconds(durationSeconds));

                log.info("ETA 계산 완료: 거리 {}, 소요시간 {}초", leg.distance.humanReadable, durationSeconds);
            }
        } catch (Exception e) {
            // 상세 에러 로그 출력 (원인 파악용)
            log.error("Google Maps API 상세 에러 레포트: ", e);
            response.setDistanceToDestination("계산 오류");
        }
    }

    /**
     * Entity -> DetailResponse DTO 변환
     */
    private ShipmentDetailResponse toDetailResponse(Shipment shipment) {
        // 화물 번호 생성: [코드]-[날짜]-[ID뒷자리] (예: GEN-260212-001)
        String shipmentNumber = String.format("%s-%s-%03d",
                shipment.getCargoType().getCode(),
                shipment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyMMdd")),
                shipment.getShipmentId() % 1000);

        return ShipmentDetailResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .shipmentNumber(shipmentNumber)
                .shipmentStatus(shipment.getShipmentStatus())
                .createdAt(shipment.getCreatedAt())
                .shipperId(shipment.getShipper().getShipperId())
                .shipperName(shipment.getShipper().getUser().getName())
                .driverId(shipment.getDriver() != null ? shipment.getDriver().getDriverId() : null)
                .driverName(shipment.getDriver() != null ? shipment.getDriver().getUser().getName() : "미배차")
                //.driverPhotoUrl(shipment.getDriver() != null ? shipment.getDriver().getUser().getProfilePhotoUrl() : null)
                .currentDriverPoint(convertToSpringPoint(shipment.getCurrentLocationPoint()))
                .pickupAddress(shipment.getPickupAddress())
                .waypoint1Address(shipment.getWaypoint1Address())
                .waypoint2Address(shipment.getWaypoint2Address())
                .dropoffAddress(shipment.getDropoffAddress())
                .cargoType(shipment.getCargoType())
                .cargoVolume(shipment.getCargoVolume())
                .cargoWeight(shipment.getCargoWeight())
                .price(shipment.getPrice())
                .platformFee(shipment.getPlatformFee())
                .profit(shipment.getProfit())
                .pickupPoint(convertToSpringPoint(shipment.getPickupPoint()))
                .dropoffPoint(convertToSpringPoint(shipment.getDropoffPoint()))
                .build();
    }

    // JTS Point를 Spring Data Point(DTO용)로 변환
    private org.springframework.data.geo.Point convertToSpringPoint(Point jtsPoint) {
        if (jtsPoint == null) return null;
        return new org.springframework.data.geo.Point(jtsPoint.getX(), jtsPoint.getY());
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
