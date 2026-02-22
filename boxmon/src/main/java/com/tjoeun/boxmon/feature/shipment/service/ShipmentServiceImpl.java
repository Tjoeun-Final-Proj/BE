package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.exception.ShipmentStateConflictException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.*;
import com.tjoeun.boxmon.feature.notification.service.NotificationUseCase;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.repository.DriverRepository;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import lombok.RequiredArgsConstructor;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal; // 정밀한 소수점 계산을 위한 BigDecimal
import java.util.stream.Collectors; // 스트림 API 컬렉터

/**
 * 운송(Shipment) 관련 비즈니스 로직을 처리하는 서비스 구현체.
 * 화물 생성, 목록 조회, 상세 조회 및 Naver Directions API를 이용한 ETA 계산 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    // 의존성 주입: Repository 및 Naver Directions API 클라이언트
    private final ShipmentRepository shipmentRepository;
    private final ShipperRepository shipperRepository;
    private final DriverRepository driverRepository;
    private final NotificationUseCase notificationUseCase;
    private final NaverDirectionsApiClient naverDirectionsApiClient;

    // JTS(Java Topology Suite) 지오메트리 객체 생성을 위한 팩토리.
    // GPS 표준 좌표계(WGS84)인 SRID 4326을 사용하도록 설정.
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional(readOnly = true)
    public ShipmentDetailResponse getShipmentAcceptDetail(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

        return toDetailResponse(shipment, false, false);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDetailResponse getSettlementShipmentDetail(Long userId, Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("배송을 찾을 수 없습니다."));

        if (shipment.getShipmentStatus() != ShipmentStatus.DONE) {
            throw new ShipmentStateConflictException("Only completed shipments can be viewed in settlement detail.");
        }

        boolean isShipper = shipment.getShipper() != null && shipment.getShipper().getShipperId().equals(userId);
        boolean isDriver = shipment.getDriver() != null && shipment.getDriver().getDriverId().equals(userId);

        if (!isShipper && !isDriver) {
            throw new RoleAccessDeniedException("Only shipment shipper or assigned driver can view this settlement detail.");
        }

        return toDetailResponse(shipment, true, true);
    }

    /**
     * 새로운 운송 요청(화물)을 생성합니다.
     *
     * @param shipperId 화주(Shipper)의 고유 ID
     * @param request   화물 생성 요청 데이터 (픽업/드랍오프 정보, 화물 정보 등)
     * @throws UserNotFoundException 주어진 화주 ID에 해당하는 화주를 찾을 수 없을 때 발생
     */
    @Override
    public void createShipment(Long shipperId, ShipmentCreateRequest request) {
        // 1. 화주 정보 조회: 요청된 shipperId로 화주 엔티티를 찾고, 없으면 예외 발생
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new UserNotFoundException("화주를 찾을 수 없습니다."));

        // 2. 좌표 변환: DTO의 Spring Point 객체를 JTS Point 객체로 변환 (DB 저장용)
        Point pickupPoint = convertToJtsPoint(request.getPickupPoint());
        Point dropoffPoint = convertToJtsPoint(request.getDropoffPoint());
        Point waypoint1Point = convertToJtsPoint(request.getWaypoint1Point());
        Point waypoint2Point = convertToJtsPoint(request.getWaypoint2Point());

        // 3. 예상 거리 계산 (Naver Directions API)
        Double estimatedDistance = calculateDistance(pickupPoint, dropoffPoint,
                Optional.ofNullable(waypoint1Point), Optional.ofNullable(waypoint2Point));


        // 4. 배송 초기 상태 및 비용 계산:
        //    - 배송 상태를 '요청됨'으로 초기화
        //    - 요청된 운임을 기반으로 플랫폼 수수료 (10%) 및 운송 기사 수익 계산
        //    - TODO: 수수료율은 현재 하드코딩되어 있으며, 추후 시스템 설정 테이블에서 관리하도록 변경 예정
        ShipmentStatus shipmentStatus = ShipmentStatus.REQUESTED;
        BigDecimal price = BigDecimal.valueOf(request.getPrice()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal platformFee = price.multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal profit = price.subtract(platformFee).setScale(0, RoundingMode.HALF_UP);

        // 5. Shipment 엔티티 빌드 및 생성: 요청 데이터를 기반으로 Shipment 엔티티를 생성
        Shipment shipment = Shipment.builder()
                .shipper(shipper)
                .pickupPoint(pickupPoint)
                .pickupAddress(request.getPickupAddress())
                .pickupDesiredAt(request.getPickupDesiredAt())
                .dropoffPoint(dropoffPoint)
                .dropoffAddress(request.getDropoffAddress())
                .dropoffDesiredAt(request.getDropoffDesiredAt())
                .waypoint1Point(waypoint1Point)
                .waypoint1Address(request.getWaypoint1Address())
                .waypoint2Point(waypoint2Point)
                .waypoint2Address(request.getWaypoint2Address())
                .estimatedDistance(estimatedDistance)
                .price(price)
                .platformFee(platformFee)
                .profit(profit)
                .cargoType(request.getCargoType())
                .cargoWeight(request.getCargoWeight())
                .cargoVolume(request.getCargoVolume())
                .vehicleType(request.getVehicleType())
                .needRefrigerate(request.getNeedRefrigerate())
                .needFreeze(request.getNeedFreeze())
                .description(request.getDescription())
                .cargoPhotoUrl(request.getCargoPhotoUrl())
                .shipmentStatus(shipmentStatus)
                .settlementStatus(SettlementStatus.INELIGIBLE) // 초기 정산 상태는 '정산 대상 아님'
                .build();

        // 6. 생성된 Shipment 엔티티를 데이터베이스에 저장
        shipmentRepository.save(shipment);
    }

    /**
     * 배차 상태의 요청 건을 배차 기사에게 할당하고 배차 완료 상태로 변경합니다.
     *
     * @param driverId 배차 기사 ID
     * @param shipmentId 배차 대상 배송 ID
     */
    @Override
    public void acceptShipment(Long driverId, Long shipmentId) {
        validateDriverAccess(driverId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

        if (shipment.getShipmentStatus() != ShipmentStatus.REQUESTED) {
            throw new ShipmentStateConflictException("Only shipments in REQUESTED status can be accepted.");
        }

        if (shipment.getDriver() != null) {
            throw new ShipmentStateConflictException("Shipment already assigned.");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RoleAccessDeniedException("Driver access required."));

        shipment.setDriver(driver);
        shipment.setAcceptedAt(LocalDateTime.now());
        shipment.setShipmentStatus(ShipmentStatus.ASSIGNED);
        shipmentRepository.save(shipment);

        try {
            notificationUseCase.notifyAssignmentCompleted(shipmentId);
        } catch (Exception e) {
            log.warn("배차 수락은 성공했지만 알림 전송은 건너뜁니다. shipmentId={}", shipmentId, e);
        }
    }

    @Override
    public void startTransport(Long driverId, Long shipmentId) {
        validateDriverAccess(driverId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("배송을 찾을 수 없습니다."));

        if (shipment.getShipmentStatus() == ShipmentStatus.IN_TRANSIT) {
            if (shipment.getDriver() == null || !shipment.getDriver().getDriverId().equals(driverId)) {
                throw new RoleAccessDeniedException("Only assigned driver can start this shipment.");
            }
            return;
        }

        if (shipment.getShipmentStatus() != ShipmentStatus.ASSIGNED) {
            throw new ShipmentStateConflictException("Only shipments in ASSIGNED status can be started.");
        }

        if (shipment.getDriver() == null || !shipment.getDriver().getDriverId().equals(driverId)) {
            throw new RoleAccessDeniedException("Only assigned driver can start this shipment.");
        }

        shipment.setPickupAt(LocalDateTime.now());
        shipment.setShipmentStatus(ShipmentStatus.IN_TRANSIT);
        shipmentRepository.save(shipment);

        try {
            notificationUseCase.notifyTransportStarted(shipmentId);
        } catch (Exception e) {
            log.warn("운송 시작은 성공했지만 알림 전송은 건너뜁니다. shipmentId={}", shipmentId, e);
        }
    }

    @Override
    public void completeTransport(Long driverId, Long shipmentId, String dropoffPhotoUrl) {
        validateDriverAccess(driverId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("배송을 찾을 수 없습니다."));

        if (shipment.getShipmentStatus() == ShipmentStatus.DONE) {
            if (shipment.getDriver() == null || !shipment.getDriver().getDriverId().equals(driverId)) {
                throw new RoleAccessDeniedException("Only assigned driver can complete this shipment.");
            }
            if (dropoffPhotoUrl != null && !dropoffPhotoUrl.isBlank()) {
                shipment.setDropoffPhotoUrl(dropoffPhotoUrl);
            }
            shipmentRepository.save(shipment);
            return;
        }

        if (shipment.getShipmentStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new ShipmentStateConflictException("Only shipments in IN_TRANSIT status can be completed.");
        }

        if (shipment.getDriver() == null || !shipment.getDriver().getDriverId().equals(driverId)) {
            throw new RoleAccessDeniedException("Only assigned driver can complete this shipment.");
        }

        shipment.setDropoffAt(LocalDateTime.now());
        shipment.setShipmentStatus(ShipmentStatus.DONE);
        if (dropoffPhotoUrl != null && !dropoffPhotoUrl.isBlank()) {
            shipment.setDropoffPhotoUrl(dropoffPhotoUrl);
        }
        shipment.setSettlementStatus(SettlementStatus.READY);
        shipmentRepository.save(shipment);

        try {
            notificationUseCase.notifyTransportCompleted(shipmentId);
        } catch (Exception e) {
            log.warn("운송 완료는 성공했지만 알림 전송은 건너뜁니다. shipmentId={}", shipmentId, e);
        }
    }

    /**
     * 특정 운송(화물)의 상세 정보를 조회합니다.
     * 차주 배차 여부 및 위치 정보 유무에 따라 다른 기준으로 예상 도착 시간(ETA)과 거리를 계산합니다.
     * - 실시간 계산 가능 시: 차주 현재 위치를 기준으로 남은 경로의 거리/시간 계산
     * - 그 외: 화주 희망 출발 시간을 기준으로 전체 경로의 총 거리/시간 계산
     *
     * @param shipmentId 조회할 운송(화물)의 고유 ID
     * @return {@link ShipmentDetailResponse} DTO
     * @throws ShipmentNotFoundException 주어진 운송 ID에 해당하는 운송건을 찾을 수 없을 때 발생
     */
    @Override
    @Transactional(readOnly = true)
    public ShipmentDetailResponse getShipmentDetail(Long shipmentId) {
        // 1. Shipment 엔티티 조회
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));

        // 2. 기본 정보 DTO 변환
        ShipmentDetailResponse response = toDetailResponse(shipment, false, false);

        // 3. ETA 및 거리 계산 분기 처리
        // 실시간 계산 조건: 차주가 배차되었고, 동시에 현재 위치 정보가 있는 경우
        if (shipment.getDriver() != null && shipment.getCurrentLocationPoint() != null) {
            log.info("차주 배차 및 위치 정보 확인. 현재 위치를 기준으로 남은 경로 ETA 계산을 시작합니다. (Shipment ID: {})", shipmentId);
            calculateRemainingEtaAndDistance(shipment, response);
        }
        // 그 외 모든 경우 (미배차, 또는 배차 후 미출발 등 위치 정보 없는 상태)
        else {
            log.info("차주 미배차 또는 위치 정보 없음. 희망 출발 시간을 기준으로 전체 경로 ETA 계산을 시작합니다. (Shipment ID: {})", shipmentId);
            calculateTotalEtaAndDistance(shipment, response);
        }

        return response;
    }

    /**
     * [전체 경로 계산] 출발지부터 목적지까지의 전체 예상 시간과 거리를 계산하고 응답 DTO에 설정합니다.
     * ETA(도착 예정 시간)는 화주의 '희망 출발 시간'을 기준으로 계산됩니다.
     *
     * @param shipment 운송 정보 엔티티
     * @param response 상세 응답 DTO (이 메서드 내에서 ETA 및 거리 정보가 업데이트됨)
     */
    private void calculateTotalEtaAndDistance(Shipment shipment, ShipmentDetailResponse response) {
        // 거리와 ETA를 한 번의 API 호출로 계산합니다.
        String start = shipment.getPickupPoint().getX() + "," + shipment.getPickupPoint().getY();
        String goal = shipment.getDropoffPoint().getX() + "," + shipment.getDropoffPoint().getY();
        List<String> waypoints = new ArrayList<>();
        if (shipment.getWaypoint1Point() != null) {
            waypoints.add(shipment.getWaypoint1Point().getX() + "," + shipment.getWaypoint1Point().getY());
        }
        if (shipment.getWaypoint2Point() != null) {
            waypoints.add(shipment.getWaypoint2Point().getX() + "," + shipment.getWaypoint2Point().getY());
        }

        naverDirectionsApiClient.getDirections(start, goal, waypoints).ifPresent(directionsResponse -> {
            if (directionsResponse.getRoute() != null && directionsResponse.getRoute().getTrafast() != null && !directionsResponse.getRoute().getTrafast().isEmpty()) {
                NaverDirectionsResponse.Summary summary = directionsResponse.getRoute().getTrafast().get(0).getSummary();
                if (summary != null) {
                    double distanceInKm = summary.getDistance() / 1000.0;
                    response.setDistanceToDestination(String.format("%.1f", distanceInKm));
                    long durationSeconds = summary.getDuration() / 1000;
                    response.setEstimatedArrivalTime(shipment.getPickupDesiredAt().plusSeconds(durationSeconds));
                    log.info("전체 경로 ETA 계산 완료: 거리 {} km, 소요시간 {}초, 기준 시간 {}", String.format("%.1f", distanceInKm), durationSeconds, shipment.getPickupDesiredAt());
                }
            }
        });
    }

    /**
     * Naver Directions API를 호출하여 출발지, 도착지, 경유지를 기반으로 예상 거리를 계산합니다.
     *
     * @param startPoint 출발지 좌표
     * @param goalPoint  도착지 좌표
     * @param waypoint1  경유지1 좌표 (Optional)
     * @param waypoint2  경유지2 좌표 (Optional)
     * @return 계산된 거리 (km), 실패 시 null 반환
     */
    private Double calculateDistance(Point startPoint, Point goalPoint, Optional<Point> waypoint1, Optional<Point> waypoint2) {
        if (startPoint == null || goalPoint == null) {
            log.warn("출발지 또는 목적지 좌표가 없어 거리 계산을 스킵합니다.");
            return null;
        }

        String start = startPoint.getX() + "," + startPoint.getY();
        String goal = goalPoint.getX() + "," + goalPoint.getY();

        List<String> waypoints = new ArrayList<>();
        waypoint1.ifPresent(p -> waypoints.add(p.getX() + "," + p.getY()));
        waypoint2.ifPresent(p -> waypoints.add(p.getX() + "," + p.getY()));

        log.info("Naver Directions API 호출 (거리 계산) - 출발지: {}, 목적지: {}, 경유지: {}", start, goal, waypoints);
        Optional<NaverDirectionsResponse> directionsResponseOptional = naverDirectionsApiClient.getDirections(start, goal, waypoints);

        return directionsResponseOptional.map(response -> {
            if (response.getRoute() != null && response.getRoute().getTrafast() != null && !response.getRoute().getTrafast().isEmpty()) {
                NaverDirectionsResponse.Summary summary = response.getRoute().getTrafast().get(0).getSummary();
                if (summary != null) {
                    double distanceInKm = summary.getDistance() / 1000.0;
                    log.info("거리 계산 완료: {} km", distanceInKm);
                    return distanceInKm;
                }
            }
            log.warn("Naver Directions API 응답에서 경로 또는 요약 정보를 찾을 수 없습니다.");
            return null;
        }).orElseGet(() -> {
            log.error("Naver Directions API 호출 실패 또는 응답 없음.");
            return null;
        });
    }



    /**
     * [남은 경로 계산] 차주의 현재 위치부터 목적지까지의 남은 예상 시간과 거리를 계산합니다.
     * ETA는 '현재 시간'을 기준으로 계산됩니다.
     *
     * @param shipment 운송 정보 엔티티 (현재 위치, 목적지, 경유지 정보 포함)
     * @param response 상세 응답 DTO (ETA 및 거리 정보가 업데이트될 대상)
     */
    private void calculateRemainingEtaAndDistance(Shipment shipment, ShipmentDetailResponse response) {
        if (shipment.getCurrentLocationPoint() == null || shipment.getDropoffPoint() == null) {
            log.warn("운송건 ID {}: 필수 좌표(현재위치 또는 목적지)가 누락되어 ETA를 계산할 수 없습니다.", shipment.getShipmentId());
            response.setDistanceToDestination(null);
            return;
        }

        String start = shipment.getCurrentLocationPoint().getX() + "," + shipment.getCurrentLocationPoint().getY();
        String goal = shipment.getDropoffPoint().getX() + "," + shipment.getDropoffPoint().getY();

        List<String> waypoints = new ArrayList<>();
        if (shipment.getWaypoint1Point() != null) {
            waypoints.add(shipment.getWaypoint1Point().getX() + "," + shipment.getWaypoint1Point().getY());
        }
        if (shipment.getWaypoint2Point() != null) {
            waypoints.add(shipment.getWaypoint2Point().getX() + "," + shipment.getWaypoint2Point().getY());
        }

        log.info("Naver Directions API 호출 (남은 경로) - 출발지: {}, 목적지: {}, 경유지: {}", start, goal, waypoints);

        Optional<NaverDirectionsResponse> directionsResponseOptional = naverDirectionsApiClient.getDirections(start, goal, waypoints);

        directionsResponseOptional.ifPresentOrElse(
                directionsResponse -> {
                    if (directionsResponse.getRoute() != null && directionsResponse.getRoute().getTrafast() != null && !directionsResponse.getRoute().getTrafast().isEmpty()) {
                        NaverDirectionsResponse.Summary summary = directionsResponse.getRoute().getTrafast().get(0).getSummary();
                        if (summary != null) {
                            response.setDistanceToDestination(String.format("%.1f", summary.getDistance() / 1000.0));
                            long durationSeconds = summary.getDuration() / 1000;
                            response.setEstimatedArrivalTime(LocalDateTime.now().plusSeconds(durationSeconds));
                            log.info("남은 경로 ETA 계산 완료: 거리 {}, 소요시간 {}초", response.getDistanceToDestination(), durationSeconds);
                        } else {
                            log.warn("Naver Directions API (남은 경로) Summary 정보가 없습니다. (Shipment ID: {})", shipment.getShipmentId());
                            response.setDistanceToDestination(null);
                        }
                    } else {
                        log.warn("Naver Directions API (남은 경로) 경로 정보가 없습니다. (Shipment ID: {})", shipment.getShipmentId());
                        response.setDistanceToDestination(null);
                    }
                },
                () -> {
                    log.error("Naver Directions API (남은 경로) 호출 실패 또는 응답 없음. (Shipment ID: {})", shipment.getShipmentId());
                    response.setDistanceToDestination(null);
                }
        );
    }

    /**
     * Shipment 엔티티를 {@link ShipmentDetailResponse} DTO로 변환합니다.
     * 화물 번호 생성 로직을 포함하며, 운송 기사 정보가 없는 경우 "미배차"로 표시합니다.
     *
     * @param shipment 변환할 Shipment 엔티티
     * @return 변환된 {@link ShipmentDetailResponse} DTO
     */
    private ShipmentDetailResponse toDetailResponse(Shipment shipment, boolean includeCargoPhotoUrl, boolean includeDropoffPhotoUrl) {
        // 화물 번호 생성: [화물종류코드]-[생성일자(YYMMDD)]-[ShipmentId 마지막 3자리] 형식
        // 예: GEN-260212-001 (General Cargo, 26년 02월 12일, Shipment ID 끝 3자리 001)
        String shipmentNumber = String.format("%s-%s-%03d",
                shipment.getCargoType().getCode(),
                shipment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyMMdd")),
                shipment.getShipmentId() % 1000);

        ShipmentDetailResponse.ShipmentDetailResponseBuilder builder = ShipmentDetailResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .shipmentNumber(shipmentNumber)
                .shipmentStatus(shipment.getShipmentStatus())
                .createdAt(shipment.getCreatedAt())
                .shipperId(shipment.getShipper().getShipperId())
                .shipperName(shipment.getShipper().getUser().getName())
                // 운송 기사 정보는 배차 여부에 따라 동적으로 설정
                .driverId(shipment.getDriver() != null ? shipment.getDriver().getDriverId() : null)
                .driverName(shipment.getDriver() != null ? shipment.getDriver().getUser().getName() : "미배차")
                //.driverPhotoUrl(shipment.getDriver() != null ? shipment.getDriver().getUser().getProfilePhotoUrl() : null)
                .currentDriverPoint(convertToSpringPoint(shipment.getCurrentLocationPoint()))
                .pickupAddress(shipment.getPickupAddress())
                .waypoint1Address(shipment.getWaypoint1Address())
                .waypoint2Address(shipment.getWaypoint2Address())
                .dropoffAddress(shipment.getDropoffAddress())
                .pickupDesiredAt(shipment.getPickupDesiredAt())
                .dropoffDesiredAt(shipment.getDropoffDesiredAt())
                .cargoType(shipment.getCargoType())
                .cargoVolume(shipment.getCargoVolume())
                .cargoWeight(shipment.getCargoWeight())
                .vehicleType(shipment.getVehicleType().getDescription())
                .description(shipment.getDescription())
                .price(roundMoney(shipment.getPrice()))
                .platformFee(roundMoney(shipment.getPlatformFee()))
                .profit(roundMoney(shipment.getProfit()))
                .pickupPoint(convertToSpringPoint(shipment.getPickupPoint()))
                .dropoffPoint(convertToSpringPoint(shipment.getDropoffPoint()));

        if (includeCargoPhotoUrl) {
            builder.cargoPhotoUrl(shipment.getCargoPhotoUrl());
        }
        if (includeDropoffPhotoUrl) {
            builder.dropoffPhotoUrl(shipment.getDropoffPhotoUrl());
        }

        return builder.build();
    }

    /**
     * JTS(Java Topology Suite) {@link Point} 객체를 Spring Data {@link org.springframework.data.geo.Point} 객체로 변환합니다.
     * 주로 DTO 반환 시 사용되는 변환 메서드입니다.
     *
     * @param jtsPoint 변환할 JTS Point 객체 (경도, 위도)
     * @return 변환된 Spring Data Point 객체 (입력값이 null인 경우 null 반환)
     */
    private org.springframework.data.geo.Point convertToSpringPoint(Point jtsPoint) {
        if (jtsPoint == null) return null;
        return new org.springframework.data.geo.Point(jtsPoint.getX(), jtsPoint.getY());
    }

    /**
     * Spring Data {@link org.springframework.data.geo.Point} 객체를 JTS(Java Topology Suite) {@link Point} 객체로 변환합니다.
     * 주로 DTO로부터 받은 좌표를 DB에 저장하기 위한 엔티티 변환 시 사용됩니다.
     * JTS는 기본적으로 X축을 경도(longitude), Y축을 위도(latitude)로 간주합니다.
     *
     * @param source 변환할 Spring Data Point 객체 (경도, 위도)
     * @return 변환된 JTS Point 객체 (입력값이 null인 경우 null 반환)
     */
    private Point convertToJtsPoint(org.springframework.data.geo.Point source) {
        if (source == null) return null;
        return geometryFactory.createPoint(new Coordinate(source.getX(), source.getY()));
    }

    private BigDecimal roundMoney(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public ShipperSettlementSummaryResponse getShipperSettlementSummary(Long shipperId) {
        // 1. 화주 접근 권한 검증: 주어진 shipperId가 유효한 화주인지 확인
        validateShipperAccess(shipperId);
        LocalDateTime now = LocalDateTime.now();

        // 2. 기간 설정: 이번 달과 지난 달의 시작일 및 종료일을 계산
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusNanos(1); // 이번 달 시작 직전까지

        // 3. 이번 달 총 운임 조회: 현재 월의 총 운임 합계 (없으면 0으로 간주)
        BigDecimal thisMonthTotal = Optional.ofNullable(
                shipmentRepository.findTotalAmountByShipperAndPeriod(shipperId, startOfThisMonth, now)
        ).orElse(BigDecimal.ZERO);

        // 4. 지난 달 총 운임 조회: 지난 달의 총 운임 합계 (없으면 0으로 간주)
        BigDecimal lastMonthTotal = Optional.ofNullable(
                shipmentRepository.findTotalAmountByShipperAndPeriod(shipperId, startOfLastMonth, endOfLastMonth)
        ).orElse(BigDecimal.ZERO);

        // 5. 응답 DTO 빌드: 조회된 금액과 전월 대비 차이를 계산하여 반환
        return ShipperSettlementSummaryResponse.builder()
                .thisMonthTotalAmount(roundMoney(thisMonthTotal))
                .lastMonthTotalAmount(roundMoney(lastMonthTotal))
                .difference(roundMoney(thisMonthTotal.subtract(lastMonthTotal)))
                .build();
    }

    @Override
    public DriverSettlementSummaryResponse getDriverSettlementSummary(Long driverId) {
        // 1. 운송 기사 접근 권한 검증: 주어진 driverId가 유효한 운송 기사인지 확인
        validateDriverAccess(driverId);
        LocalDateTime now = LocalDateTime.now();

        // 2. 기간 설정: 이번 달과 지난 달의 시작일 및 종료일을 계산
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusNanos(1); // 이번 달 시작 직전까지

        // 3. 이번 달 총 수익 조회: 현재 월의 총 수익 합계 (없으면 0으로 간주)
        BigDecimal thisMonthProfit = Optional.ofNullable(
                shipmentRepository.findTotalProfitByDriverAndPeriod(driverId, startOfThisMonth, now)
        ).orElse(BigDecimal.ZERO);

        // 4. 지난 달 총 수익 조회: 지난 달의 총 수익 합계 (없으면 0으로 간주)
        BigDecimal lastMonthProfit = Optional.ofNullable(
                shipmentRepository.findTotalProfitByDriverAndPeriod(driverId, startOfLastMonth, endOfLastMonth)
        ).orElse(BigDecimal.ZERO);

        // 5. 응답 DTO 빌드: 조회된 수익과 전월 대비 차이를 계산하여 반환
        return DriverSettlementSummaryResponse.builder()
                .thisMonthTotalProfit(roundMoney(thisMonthProfit))
                .lastMonthTotalProfit(roundMoney(lastMonthProfit))
                .difference(roundMoney(thisMonthProfit.subtract(lastMonthProfit)))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipperSettlementListResponse> getShipperSettlementList(
            Long shipperId,
            int year,
            int month,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        // 1. 화주 접근 권한 및 연/월 유효성 검증
        validateShipperAccess(shipperId);
        validateYearMonth(year, month);

        // 2. 조회 기간 설정: 주어진 연도와 월의 시작일과 종료일 계산
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1); // 다음 달 시작 전까지

        // 3. 조건에 맞는 배송 목록 조회: 배송 상태 및 정산 상태 필터를 적용하여 Repository에서 배송 엔티티 목록 조회
        List<Shipment> shipments = findShipperSettlementShipments(
                shipperId, start, end, shipmentStatus, settlementStatus
        );

        // 4. DTO 변환: 조회된 Shipment 엔티티 목록을 ShipperSettlementListResponse DTO 목록으로 변환하여 반환
        return shipments.stream()
                .map(this::toShipperSettlementListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverSettlementListResponse> getDriverSettlementList(
            Long driverId,
            int year,
            int month,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        // 1. 운송 기사 접근 권한 및 연/월 유효성 검증
        validateDriverAccess(driverId);
        validateYearMonth(year, month);

        // 2. 조회 기간 설정: 주어진 연도와 월의 시작일과 종료일 계산
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1); // 다음 달 시작 전까지

        // 3. 조건에 맞는 배송 목록 조회: 배송 상태 및 정산 상태 필터를 적용하여 Repository에서 배송 엔티티 목록 조회
        List<Shipment> shipments = findDriverSettlementShipments(
                driverId, start, end, shipmentStatus, settlementStatus
        );

        // 4. DTO 변환: 조회된 Shipment 엔티티 목록을 DriverSettlementListResponse DTO 목록으로 변환하여 반환
        return shipments.stream()
                .map(this::toDriverSettlementListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 입력된 연도(year)와 월(month)의 유효성을 검사합니다.
     * 유효하지 않은 값이면 {@link IllegalArgumentException}을 발생시킵니다.
     *
     * @param year 검사할 연도
     * @param month 검사할 월
     * @throws IllegalArgumentException 연도나 월이 유효하지 않을 경우
     */
    private void validateYearMonth(int year, int month) {
        if (year < 1) {
            throw new IllegalArgumentException("year must be a positive integer.");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12.");
        }
    }

    /**
     * 특정 화주 ID의 존재 여부를 검증하고, 존재하지 않으면 {@link RoleAccessDeniedException}을 발생시킵니다.
     *
     * @param shipperId 검사할 화주 ID
     * @throws RoleAccessDeniedException 해당 화주 ID에 대한 접근 권한이 없거나 화주가 존재하지 않을 경우
     */
    private void validateShipperAccess(Long shipperId) {
        if (!shipperRepository.existsById(shipperId)) {
            throw new RoleAccessDeniedException("Shipper access required.");
        }
    }

    /**
     * 특정 운송 기사 ID의 존재 여부를 검증하고, 존재하지 않으면 {@link RoleAccessDeniedException}을 발생시킵니다.
     *
     * @param driverId 검사할 운송 기사 ID
     * @throws RoleAccessDeniedException 해당 운송 기사 ID에 대한 접근 권한이 없거나 운송 기사가 존재하지 않을 경우
     */
    private void validateDriverAccess(Long driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new RoleAccessDeniedException("Driver access required.");
        }
    }

    /**
     * 화주 정산 목록 조회를 위해 다양한 필터 조건(배송 상태, 정산 상태)에 따라 배송 엔티티를 조회합니다.
     * 필터 조건의 조합에 따라 적절한 Repository 메서드를 호출합니다.
     *
     * @param shipperId 화주 ID
     * @param start 조회 시작일시
     * @param end 조회 종료일시
     * @param shipmentStatus 배송 상태 (선택 사항)
     * @param settlementStatus 정산 상태 (선택 사항)
     * @return 필터링된 배송 엔티티 목록
     */
    private List<Shipment> findShipperSettlementShipments(
            Long shipperId,
            LocalDateTime start,
            LocalDateTime end,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        // 배송 상태와 정산 상태가 모두 지정된 경우
        if (shipmentStatus != null && settlementStatus != null) {
            return shipmentRepository
                    .findByShipper_ShipperIdAndCreatedAtBetweenAndShipmentStatusAndSettlementStatusOrderByCreatedAtDesc(
                            shipperId, start, end, shipmentStatus, settlementStatus
                    );
        }
        // 배송 상태만 지정된 경우
        if (shipmentStatus != null) {
            return shipmentRepository
                    .findByShipper_ShipperIdAndCreatedAtBetweenAndShipmentStatusOrderByCreatedAtDesc(
                            shipperId, start, end, shipmentStatus
                    );
        }
        // 정산 상태만 지정된 경우
        if (settlementStatus != null) {
            return shipmentRepository
                    .findByShipper_ShipperIdAndCreatedAtBetweenAndSettlementStatusOrderByCreatedAtDesc(
                            shipperId, start, end, settlementStatus
                    );
        }
        // 아무런 필터 조건도 지정되지 않은 경우
        return shipmentRepository
                .findByShipper_ShipperIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        shipperId, start, end
                );
    }

    /**
     * 운송 기사 정산 목록 조회를 위해 다양한 필터 조건(배송 상태, 정산 상태)에 따라 배송 엔티티를 조회합니다.
     * 필터 조건의 조합에 따라 적절한 Repository 메서드를 호출합니다.
     *
     * @param driverId 운송 기사 ID
     * @param start 조회 시작일시
     * @param end 조회 종료일시
     * @param shipmentStatus 배송 상태 (선택 사항)
     * @param settlementStatus 정산 상태 (선택 사항)
     * @return 필터링된 배송 엔티티 목록
     */
    private List<Shipment> findDriverSettlementShipments(
            Long driverId,
            LocalDateTime start,
            LocalDateTime end,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        // 배송 상태와 정산 상태가 모두 지정된 경우
        if (shipmentStatus != null && settlementStatus != null) {
            return shipmentRepository
                    .findByDriver_DriverIdAndCreatedAtBetweenAndShipmentStatusAndSettlementStatusOrderByCreatedAtDesc(
                            driverId, start, end, shipmentStatus, settlementStatus
                    );
        }
        // 배송 상태만 지정된 경우
        if (shipmentStatus != null) {
            return shipmentRepository
                    .findByDriver_DriverIdAndCreatedAtBetweenAndShipmentStatusOrderByCreatedAtDesc(
                            driverId, start, end, shipmentStatus
                    );
        }
        // 정산 상태만 지정된 경우
        if (settlementStatus != null) {
            return shipmentRepository
                    .findByDriver_DriverIdAndCreatedAtBetweenAndSettlementStatusOrderByCreatedAtDesc(
                            driverId, start, end, settlementStatus
                    );
        }
        // 아무런 필터 조건도 지정되지 않은 경우
        return shipmentRepository
                .findByDriver_DriverIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        driverId, start, end
                );
    }

    /**
     * Shipment 엔티티를 {@link ShipperSettlementListResponse} DTO로 변환합니다.
     * 화주 정산 목록 조회 시 사용됩니다.
     *
     * @param shipment 변환할 Shipment 엔티티
     * @return 변환된 {@link ShipperSettlementListResponse} DTO
     */
    private ShipperSettlementListResponse toShipperSettlementListResponse(Shipment shipment) {
        return ShipperSettlementListResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .shipmentStatus(shipment.getShipmentStatus())
                .settlementStatus(shipment.getSettlementStatus())
                .createdAt(shipment.getCreatedAt())
                .pickupDesiredAt(shipment.getPickupDesiredAt())
                .dropoffDesiredAt(shipment.getDropoffDesiredAt())
                .pickupAddress(shipment.getPickupAddress())
                .dropoffAddress(shipment.getDropoffAddress())
                .price(roundMoney(shipment.getPrice()))
                .build();
    }

    /**
     * Shipment 엔티티를 {@link DriverSettlementListResponse} DTO로 변환합니다.
     * 운송 기사 정산 목록 조회 시 사용됩니다.
     *
     * @param shipment 변환할 Shipment 엔티티
     * @return 변환된 {@link DriverSettlementListResponse} DTO
     */
    private DriverSettlementListResponse toDriverSettlementListResponse(Shipment shipment) {
        return DriverSettlementListResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .shipmentStatus(shipment.getShipmentStatus())
                .settlementStatus(shipment.getSettlementStatus())
                .createdAt(shipment.getCreatedAt())
                .pickupDesiredAt(shipment.getPickupDesiredAt())
                .dropoffDesiredAt(shipment.getDropoffDesiredAt())
                .pickupAddress(shipment.getPickupAddress())
                .dropoffAddress(shipment.getDropoffAddress())
                .profit(roundMoney(shipment.getProfit()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnassignedShipmentResponse> getUnassignedShipments() {
        List<Shipment> shipments = shipmentRepository.findByShipmentStatusOrderByCreatedAtDesc(ShipmentStatus.REQUESTED);
        return shipments.stream()
                .map(this::toUnassignedShipmentResponse)
                .collect(Collectors.toList());
    }

    private UnassignedShipmentResponse toUnassignedShipmentResponse(Shipment shipment) {
        return UnassignedShipmentResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .pickupAddress(shipment.getPickupAddress())
                .dropoffAddress(shipment.getDropoffAddress())
                .pickupDesiredAt(shipment.getPickupDesiredAt())
                .dropoffDesiredAt(shipment.getDropoffDesiredAt())
                .estimatedDistance(shipment.getEstimatedDistance())
                .cargoWeight(shipment.getCargoWeight())
                .vehicleType(shipment.getVehicleType().getDescription())
                .description(shipment.getDescription())
                .profit(roundMoney(shipment.getProfit()))
                .build();
    }
}
