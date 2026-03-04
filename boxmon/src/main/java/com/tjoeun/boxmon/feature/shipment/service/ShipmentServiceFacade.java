package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.feature.settlement.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.DriverInventoryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.DriverSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.DriverSettlementSummaryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperInventoryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperSettlementSummaryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.UnassignedShipmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * ShipmentService의 단일 진입점(Facade) 구현체.
 * 컨트롤러와 서비스 계약은 이 클래스로 유지하고,
 * 실제 비즈니스 로직은 기능군별 도메인 서비스로 위임합니다.
 */
public class ShipmentServiceFacade implements ShipmentService {

    // 생성/상태 전이(배차수락, 운송시작, 운송완료) 담당
    private final ShipmentCommandService shipmentCommandService;
    // 취소/철회 및 결제 취소 연동 담당
    private final ShipmentCancelService shipmentCancelService;
    // 상세/목록 조회 및 ETA 계산 담당
    private final ShipmentQueryService shipmentQueryService;
    // 정산 요약/목록/상세 담당
    private final ShipmentSettlementService shipmentSettlementService;

    // Command 계열 위임
    @Override
    public Long createShipment(Long shipperId, ShipmentCreateRequest request, MultipartFile cargoPhoto) {
        return shipmentCommandService.createShipment(shipperId, request, cargoPhoto);
    }

    @Override
    public void acceptShipment(Long driverId, Long shipmentId) {
        shipmentCommandService.acceptShipment(driverId, shipmentId);
    }

    @Override
    public void startTransport(Long driverId, Long shipmentId) {
        shipmentCommandService.startTransport(driverId, shipmentId);
    }

    @Override
    public void completeTransport(Long driverId, Long shipmentId, MultipartFile dropoffPhoto) {
        shipmentCommandService.completeTransport(driverId, shipmentId, dropoffPhoto);
    }

    // Cancel 계열 위임
    @Override
    public void cancelShipment(Long userId, Long shipmentId) {
        shipmentCancelService.cancelShipment(userId, shipmentId);
    }

    @Override
    public void withdrawShipmentCancel(Long userId, Long shipmentId) {
        shipmentCancelService.withdrawShipmentCancel(userId, shipmentId);
    }

    // Query 계열 위임
    @Override
    public ShipmentDetailResponse getShipmentDetail(Long shipmentId) {
        return shipmentQueryService.getShipmentDetail(shipmentId);
    }

    @Override
    public ShipmentDetailResponse getShipmentAcceptDetail(Long shipmentId) {
        return shipmentQueryService.getShipmentAcceptDetail(shipmentId);
    }

    @Override
    public ShipmentDetailResponse getSettlementShipmentDetail(Long userId, Long shipmentId) {
        return shipmentSettlementService.getSettlementShipmentDetail(userId, shipmentId);
    }

    @Override
    public List<UnassignedShipmentResponse> getUnassignedShipments() {
        return shipmentQueryService.getUnassignedShipments();
    }

    @Override
    public List<UnassignedShipmentResponse> getMyUnassignedShipments(Long shipperId) {
        return shipmentQueryService.getMyUnassignedShipments(shipperId);
    }

    @Override
    public List<ShipperInventoryResponse> getMyShipperInventory(Long shipperId) {
        return shipmentQueryService.getMyShipperInventory(shipperId);
    }

    @Override
    public List<DriverInventoryResponse> getMyDriverInventory(Long driverId) {
        return shipmentQueryService.getMyDriverInventory(driverId);
    }

    // Settlement 계열 위임
    @Override
    public ShipperSettlementSummaryResponse getShipperSettlementSummary(Long shipperId) {
        return shipmentSettlementService.getShipperSettlementSummary(shipperId);
    }

    @Override
    public DriverSettlementSummaryResponse getDriverSettlementSummary(Long driverId) {
        return shipmentSettlementService.getDriverSettlementSummary(driverId);
    }

    @Override
    public List<ShipperSettlementListResponse> getShipperSettlementList(
            Long shipperId,
            int year,
            int month,
            ShipmentStatus shipmentStatus,
            SettlementStatus settlementStatus
    ) {
        return shipmentSettlementService
                .getShipperSettlementList(shipperId, year, month, shipmentStatus, settlementStatus);
    }

    @Override
    public List<DriverSettlementListResponse> getDriverSettlementList(
            Long driverId,
            int year,
            int month,
            ShipmentStatus shipmentStatus
    ) {
        return shipmentSettlementService
                .getDriverSettlementList(driverId, year, month, shipmentStatus);
    }
}
