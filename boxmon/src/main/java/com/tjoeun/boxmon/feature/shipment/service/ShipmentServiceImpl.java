package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.exception.UserNotFoundException; // createShipment에서 UserNotFoundException을 사용하고 있으므로 유지
import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.*;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Coordinate; // JTS 좌표 객체
import org.locationtech.jts.geom.GeometryFactory; // JTS 지오메트리 객체 생성 팩토리
import org.locationtech.jts.geom.Point; // JTS 포인트 객체 (엔티티의 타입과 일치)
import org.locationtech.jts.geom.PrecisionModel; // JTS 정밀 모델 (좌표 정밀도 관리)

import com.tjoeun.boxmon.global.naver.api.NaverDirectionsApiClient; // Naver Directions API Client import
import com.tjoeun.boxmon.global.naver.dto.NaverDirectionsResponse; // Naver Directions Response DTO import

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal; // 정밀한 소수점 계산을 위한 BigDecimal
import java.util.stream.Collectors; // 스트림 API 컬렉터

/**
 * 운송(Shipment) 관련 비즈니스 로직을 처리하는 서비스 구현체.
 * 화물 생성, 목록 조회, 상세 조회 및 Google Maps API를 이용한 ETA 계산 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    @Value("${naver.maps.client-id}")
    private String naverMapsClientId;

    @Value("${naver.maps.client-secret}")
    private String naverMapsClientSecret;

    private final ShipmentRepository shipmentRepository;
    private final ShipperRepository shipperRepository;
    private final NaverDirectionsApiClient naverDirectionsApiClient;

    // GPS 표준 좌표계(WGS84)인 SRID 4326을 사용하는 Factory 생성
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 새로운 운송 요청(화물)을 생성합니다.
     *
     * @param shipperId 화주(Shipper)의 고유 ID
     * @param request   화물 생성 요청 데이터 (픽업/드랍오프 정보, 화물 정보 등)
     * @throws UserNotFoundException 주어진 화주 ID에 해당하는 화주를 찾을 수 없을 때 발생
     */
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
                .settlementStatus(SettlementStatus.INELIGIBLE)
                .build();

        // 5. 저장
        shipmentRepository.save(shipment);
    }

    /**
     * 특정 화주(Shipper)의 운송(화물) 목록을 조회합니다.
     * 상태(status)에 따라 필터링하고, 최신 생성일 기준으로 정렬하여 반환합니다.
     *
     * @param shipperId 화주(Shipper)의 고유 ID
     * @param status    조회할 운송 상태 (예: REQUESTED, IN_TRANSIT 등). null이면 모든 상태 조회.
     * @return {@link ShipmentListResponse} DTO 리스트
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
     * Shipment 엔티티를 {@link ShipmentListResponse} DTO로 변환합니다.
     *
     * @param shipment 변환할 Shipment 엔티티
     * @return 변환된 {@link ShipmentListResponse} DTO
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
     * 특정 운송(화물)의 상세 정보를 조회합니다.
     * 차주가 배차되었고 현재 위치 정보가 있는 경우, Google Directions API를 사용하여 예상 도착 시간(ETA)과 남은 거리를 계산합니다.
     *
     * @param shipmentId 조회할 운송(화물)의 고유 ID
     * @return {@link ShipmentDetailResponse} DTO
     * @throws ShipmentNotFoundException 주어진 운송 ID에 해당하는 운송건을 찾을 수 없을 때 발생
     */
    @Override
    @Transactional(readOnly = true)
    public ShipmentDetailResponse getShipmentDetail(Long shipmentId) {
        // 1. 엔티티 조회
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

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
     * Naver Directions 5 API를 호출하여 현재 위치부터 목적지까지의 예상 도착 시간(ETA)과 거리를 계산합니다.
     * 경유지를 포함하여 거리를 계산하고, 계산된 정보는 {@link ShipmentDetailResponse}에 설정됩니다.
     *
     * @param shipment 운송 정보 엔티티
     * @param response 상세 응답 DTO
     */
    private void calculateEtaAndDistance(Shipment shipment, ShipmentDetailResponse response) {
        // 1. 방어 로직: 필수 좌표가 없으면 중단 (NPE 방지)
        if (shipment.getCurrentLocationPoint() == null || shipment.getDropoffPoint() == null) {
            log.warn("운송건 ID {}: 필수 좌표(현재위치 또는 목적지)가 누락되어 ETA를 계산할 수 없습니다.", shipment.getShipmentId());
            response.setDistanceToDestination("좌표 누락");
            return;
        }

        // 2. 좌표 변환 (JTS Point -> "경도,위도" 문자열)
        // 네이버 API는 "경도,위도" 순서를 따름
        String start = shipment.getCurrentLocationPoint().getX() + "," + shipment.getCurrentLocationPoint().getY();
        String goal = shipment.getDropoffPoint().getX() + "," + shipment.getDropoffPoint().getY();

        List<String> waypoints = new ArrayList<>();
        if (shipment.getWaypoint1Point() != null) {
            waypoints.add(shipment.getWaypoint1Point().getX() + "," + shipment.getWaypoint1Point().getY());
        }
        if (shipment.getWaypoint2Point() != null) {
            waypoints.add(shipment.getWaypoint2Point().getX() + "," + shipment.getWaypoint2Point().getY());
        }

        log.info("Naver Directions API 호출 - 출발지: {}, 목적지: {}, 경유지: {}", start, goal, waypoints);

        Optional<NaverDirectionsResponse> directionsResponseOptional = naverDirectionsApiClient.getDirections(start, goal, waypoints);

        if (directionsResponseOptional.isPresent()) {
            NaverDirectionsResponse directionsResponse = directionsResponseOptional.get();
            // 응답에서 가장 적합한 경로 (예: trafast)를 가져와 요약 정보를 추출합니다.
            // 네이버 API는 여러 경로 옵션을 제공할 수 있으며, 여기서는 첫 번째 옵션인 trafast를 가정합니다.
            // 실제 사용 시 어떤 경로를 선택할지 비즈니스 로직에 따라 결정해야 합니다.
            if (directionsResponse.getRoute() != null && directionsResponse.getRoute().getTrafast() != null && !directionsResponse.getRoute().getTrafast().isEmpty()) {
                NaverDirectionsResponse.Summary summary = directionsResponse.getRoute().getTrafast().get(0).getSummary();

                if (summary != null) {
                    // 남은 거리 설정 (미터 단위를 km로 변환)
                    response.setDistanceToDestination(String.format("%.1f km", summary.getDistance() / 1000.0));

                    // 예상 도착 시간 계산 (현재 서버 시간 + 소요 밀리초)
                    long durationSeconds = summary.getDuration() / 1000;
                    response.setEstimatedArrivalTime(LocalDateTime.now().plusSeconds(durationSeconds));

                    log.info("ETA 계산 완료: 거리 {}, 소요시간 {}초", response.getDistanceToDestination(), durationSeconds);
                } else {
                    log.warn("Naver Directions API Summary 정보가 없습니다. (Shipment ID: {})", shipment.getShipmentId());
                    response.setDistanceToDestination("경로 요약 없음");
                }
            } else {
                log.warn("Naver Directions API 경로 정보가 없습니다. (Shipment ID: {})", shipment.getShipmentId());
                response.setDistanceToDestination("경로 없음");
            }
        } else {
            log.error("Naver Directions API 호출 실패 또는 응답 없음. (Shipment ID: {})", shipment.getShipmentId());
            response.setDistanceToDestination("계산 오류");
        }
    }

    /**
     * Shipment 엔티티를 {@link ShipmentDetailResponse} DTO로 변환합니다.
     * 화물 번호를 생성하고, 드라이버 정보 등을 포함합니다.
     *
     * @param shipment 변환할 Shipment 엔티티
     * @return 변환된 {@link ShipmentDetailResponse} DTO
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

    /**
     * JTS(Java Topology Suite) {@link Point} 객체를 Spring Data {@link org.springframework.data.geo.Point} 객체로 변환합니다.
     * DTO에서 사용하기 위한 변환 메서드입니다.
     *
     * @param jtsPoint 변환할 JTS Point 객체
     * @return 변환된 Spring Data Point 객체 (null인 경우 null 반환)
     */
    private org.springframework.data.geo.Point convertToSpringPoint(Point jtsPoint) {
        if (jtsPoint == null) return null;
        return new org.springframework.data.geo.Point(jtsPoint.getX(), jtsPoint.getY());
    }

    /**
     * Spring Data {@link org.springframework.data.geo.Point} 객체를 JTS(Java Topology Suite) {@link Point} 객체로 변환합니다.
     * DTO에 담긴 x, y 좌표를 JTS 전용 바이너리 객체로 변환하는 데 사용됩니다.
     * JTS는 기본적으로 x=경도(longitude), y=위도(latitude) 순서를 따릅니다.
     *
     * @param source 변환할 Spring Data Point 객체
     * @return 변환된 JTS Point 객체 (null인 경우 null 반환)
     */
    private Point convertToJtsPoint(org.springframework.data.geo.Point source) {
        if (source == null) return null;
        // JTS는 기본적으로 x=경도(longitude), y=위도(latitude) 순서를 따릅니다.
        return geometryFactory.createPoint(new Coordinate(source.getX(), source.getY()));
    }

    public ShipperSettlementSummaryResponse getShipperSettlementSummary(Long shipperId) {
        LocalDateTime now = LocalDateTime.now();

        // 이번 달 시작일 ~ 현재
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        // 저번 달 시작일 ~ 저번 달 마지막일
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusNanos(1);

        BigDecimal thisMonthTotal = Optional.ofNullable(
                shipmentRepository.findTotalAmountByShipperAndPeriod(shipperId, startOfThisMonth, now)
        ).orElse(BigDecimal.ZERO);

        BigDecimal lastMonthTotal = Optional.ofNullable(
                shipmentRepository.findTotalAmountByShipperAndPeriod(shipperId, startOfLastMonth, endOfLastMonth)
        ).orElse(BigDecimal.ZERO);

        return ShipperSettlementSummaryResponse.builder()
                .thisMonthTotalAmount(thisMonthTotal)
                .lastMonthTotalAmount(lastMonthTotal)
                .difference(thisMonthTotal.subtract(lastMonthTotal))
                .build();
    }

    public DriverSettlementSummaryResponse getDriverSettlementSummary(Long driverId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusNanos(1);

        // profit 컬럼을 합산하도록 변경
        BigDecimal thisMonthProfit = Optional.ofNullable(
                shipmentRepository.findTotalProfitByDriverAndPeriod(driverId, startOfThisMonth, now)
        ).orElse(BigDecimal.ZERO);

        BigDecimal lastMonthProfit = Optional.ofNullable(
                shipmentRepository.findTotalProfitByDriverAndPeriod(driverId, startOfLastMonth, endOfLastMonth)
        ).orElse(BigDecimal.ZERO);

        return DriverSettlementSummaryResponse.builder()
                .thisMonthTotalProfit(thisMonthProfit)
                .lastMonthTotalProfit(lastMonthProfit)
                .difference(thisMonthProfit.subtract(lastMonthProfit))
                .build();
    }
}
