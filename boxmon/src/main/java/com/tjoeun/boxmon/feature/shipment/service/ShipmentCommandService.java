package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.exception.ShipmentStateConflictException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.admin.service.SystemSettingService;
import com.tjoeun.boxmon.feature.notification.service.NotificationUseCase;
import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.mapper.ShipmentCreateMapper;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.repository.DriverRepository;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import com.tjoeun.boxmon.global.naver.api.NaverDirectionsApiClient;
import com.tjoeun.boxmon.global.naver.dto.NaverDirectionsResponse;
import com.tjoeun.boxmon.global.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentCommandService {

    private final ShipmentRepository shipmentRepository;
    private final ShipperRepository shipperRepository;
    private final DriverRepository driverRepository;
    private final SystemSettingService systemSettingService;
    private final NotificationUseCase notificationUseCase;
    private final NaverDirectionsApiClient naverDirectionsApiClient;
    private final ObjectStorageService objectStorageService;
    private final ShipmentDomainSupport support;
    private final ShipmentCreateMapper shipmentCreateMapper;

    /**
     * 새로운 운송 요청(화물)을 생성합니다.
     * 기존 ShipmentServiceImpl의 생성 로직을 그대로 분리한 구현입니다.
     *
     * @param shipperId 화주(Shipper)의 고유 ID
     * @param request 화물 생성 요청 데이터
     * @param cargoPhoto 화물 사진 파일(선택)
     * @return 생성된 화물 ID
     */
    public Long createShipment(Long shipperId, ShipmentCreateRequest request, MultipartFile cargoPhoto) {
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new UserNotFoundException("화주를 찾을 수 없습니다."));

        Point pickupPoint = support.convertToJtsPoint(request.getPickupPoint());
        Point dropoffPoint = support.convertToJtsPoint(request.getDropoffPoint());
        Point waypoint1Point = support.convertToJtsPoint(request.getWaypoint1Point());
        Point waypoint2Point = support.convertToJtsPoint(request.getWaypoint2Point());

        // Naver Directions API를 통해 예상 거리(km)를 계산합니다.
        Double estimatedDistance = calculateDistance(pickupPoint, dropoffPoint,
                Optional.ofNullable(waypoint1Point), Optional.ofNullable(waypoint2Point));

        String uploadedCargoPhotoKey = null;
        String cargoPhotoUrl = null;
        if (cargoPhoto != null && !cargoPhoto.isEmpty()) {
            // 이미지 업로드 성공 시 DB에는 공개 URL만 저장합니다.
            uploadedCargoPhotoKey = objectStorageService.uploadCargoPhoto(cargoPhoto);
            cargoPhotoUrl = objectStorageService.buildPublicUrl(uploadedCargoPhotoKey);
        }

        BigDecimal price = BigDecimal.valueOf(request.getPrice()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal feeRate = systemSettingService.getFeeRateOrDefault();
        BigDecimal platformFee = price.multiply(feeRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal profit = price.subtract(platformFee).setScale(0, RoundingMode.HALF_UP);

        Shipment shipment = shipmentCreateMapper.toShipment(
                shipper, request, pickupPoint, dropoffPoint, waypoint1Point, waypoint2Point, estimatedDistance, price, platformFee, profit, cargoPhotoUrl, support.normalizeCompanyName(request.getCompanyName())
        );

        try {
            Shipment savedShipment = shipmentRepository.save(shipment);
            return savedShipment.getShipmentId();
        } catch (RuntimeException e) {
            // 업로드 후 DB 저장 실패 시 고아 파일을 남기지 않기 위한 보상 삭제
            if (uploadedCargoPhotoKey != null) {
                objectStorageService.deleteObject(uploadedCargoPhotoKey);
            }
            throw e;
        }
    }

    /**
     * REQUESTED 상태 화물을 배차 수락 처리합니다.
     */
    public void acceptShipment(Long driverId, Long shipmentId) {
        support.validateDriverAccess(driverId);

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
            // 핵심 트랜잭션(배차 수락)은 유지하고 알림 실패는 경고 로그로만 처리
            notificationUseCase.notifyAssignmentCompleted(shipmentId);
        } catch (Exception e) {
            log.warn("배차 수락은 성공했지만 알림 전송은 건너뜁니다. shipmentId={}", shipmentId, e);
        }
    }

    /**
     * ASSIGNED 상태 화물을 운송 시작(IN_TRANSIT)으로 전환합니다.
     */
    public void startTransport(Long driverId, Long shipmentId) {
        support.validateDriverAccess(driverId);

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
            // 핵심 트랜잭션(운송 시작)은 유지하고 알림 실패는 경고 로그로만 처리
            notificationUseCase.notifyTransportStarted(shipmentId);
        } catch (Exception e) {
            log.warn("운송 시작은 성공했지만 알림 전송은 건너뜁니다. shipmentId={}", shipmentId, e);
        }
    }

    /**
     * IN_TRANSIT 상태 화물을 운송 완료(DONE) 처리합니다.
     * 하차 사진이 있으면 업로드 후 URL을 저장합니다.
     */
    public void completeTransport(Long driverId, Long shipmentId, MultipartFile dropoffPhoto) {
        support.validateDriverAccess(driverId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("배송을 찾을 수 없습니다."));

        String uploadedDropoffPhotoKey = null;
        String dropoffPhotoUrl = null;
        if (dropoffPhoto != null && !dropoffPhoto.isEmpty()) {
            // 하차 사진 업로드 후 공개 URL 저장
            uploadedDropoffPhotoKey = objectStorageService.uploadDropoffPhoto(dropoffPhoto);
            dropoffPhotoUrl = objectStorageService.buildPublicUrl(uploadedDropoffPhotoKey);
        }

        if (shipment.getShipmentStatus() == ShipmentStatus.DONE) {
            if (shipment.getDriver() == null || !shipment.getDriver().getDriverId().equals(driverId)) {
                throw new RoleAccessDeniedException("Only assigned driver can complete this shipment.");
            }
            if (dropoffPhotoUrl != null && !dropoffPhotoUrl.isBlank()) {
                shipment.setDropoffPhotoUrl(dropoffPhotoUrl);
            }
            try {
                shipmentRepository.save(shipment);
            } catch (RuntimeException e) {
                // 이미지 업로드 보상 삭제
                if (uploadedDropoffPhotoKey != null) {
                    objectStorageService.deleteObject(uploadedDropoffPhotoKey);
                }
                throw e;
            }
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
        try {
            shipmentRepository.save(shipment);
        } catch (RuntimeException e) {
            if (uploadedDropoffPhotoKey != null) {
                objectStorageService.deleteObject(uploadedDropoffPhotoKey);
            }
            throw e;
        }

        try {
            // 핵심 트랜잭션(운송 완료)은 유지하고 알림 실패는 경고 로그로만 처리
            notificationUseCase.notifyTransportCompleted(shipmentId);
        } catch (Exception e) {
            log.warn("운송 완료는 성공했지만 알림 전송은 건너뜁니다. shipmentId={}", shipmentId, e);
        }
    }

    /**
     * Naver Directions API를 이용해 예상 거리(km)를 계산합니다.
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
}
