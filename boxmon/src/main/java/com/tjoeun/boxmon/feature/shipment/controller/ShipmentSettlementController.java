package com.tjoeun.boxmon.feature.shipment.controller;

import com.tjoeun.boxmon.feature.shipment.domain.SettlementStatus;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.dto.DriverSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.DriverSettlementSummaryResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperSettlementListResponse;
import com.tjoeun.boxmon.feature.shipment.dto.ShipperSettlementSummaryResponse;
import com.tjoeun.boxmon.feature.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 배송 정산과 관련된 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 화주 및 운송 기사의 정산 목록 조회 및 요약 정보를 제공합니다.
 */
@Tag(name = "정산", description = "화주 및 운송 기사의 정산 관련 API")
@RestController
@RequestMapping("/api/shipments/settlements")
@RequiredArgsConstructor
public class ShipmentSettlementController {

    private final ShipmentService shipmentService;

    /**
     * 화주의 정산 목록을 조회합니다.
     *
     * @param authentication 현재 인증된 사용자의 정보 (화주 ID 추출)
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param shipmentStatus (선택 사항) 배송 상태 필터
     * @param settlementStatus (선택 사항) 정산 상태 필터
     * @return 화주 정산 목록과 HTTP 200 OK 응답
     */
    @Operation(summary = "화주 정산 목록 조회", description = "인증된 화주의 정산 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "정산 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ShipperSettlementListResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/shipper")
    public ResponseEntity<List<ShipperSettlementListResponse>> getShipperSettlementList(
            Authentication authentication,
            @Parameter(description = "조회할 연도", example = "2023") @RequestParam(name = "year") int year,
            @Parameter(description = "조회할 월", example = "10") @RequestParam(name = "month") int month,
            @Parameter(description = "배송 상태 필터 (선택 사항)", example = "DELIVERED") @RequestParam(name = "shipmentStatus", required = false) ShipmentStatus shipmentStatus,
            @Parameter(description = "정산 상태 필터 (선택 사항)", example = "COMPLETED") @RequestParam(name = "settlementStatus", required = false) SettlementStatus settlementStatus
    ) {
        Long shipperId = Long.valueOf(authentication.getPrincipal().toString());
        List<ShipperSettlementListResponse> responses =
                shipmentService.getShipperSettlementList(shipperId, year, month, shipmentStatus, settlementStatus);
        return ResponseEntity.ok(responses);
    }

    /**
     * 운송 기사의 정산 목록을 조회합니다.
     *
     * @param authentication 현재 인증된 사용자의 정보 (운송 기사 ID 추출)
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param shipmentStatus (선택 사항) 배송 상태 필터
     * @param settlementStatus (선택 사항) 정산 상태 필터
     * @return 운송 기사 정산 목록과 HTTP 200 OK 응답
     */
    @Operation(summary = "운송 기사 정산 목록 조회", description = "인증된 운송 기사의 정산 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "정산 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = DriverSettlementListResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/driver")
    public ResponseEntity<List<DriverSettlementListResponse>> getDriverSettlementList(
            Authentication authentication,
            @Parameter(description = "조회할 연도", example = "2023") @RequestParam(name = "year") int year,
            @Parameter(description = "조회할 월", example = "10") @RequestParam(name = "month") int month,
            @Parameter(description = "배송 상태 필터 (선택 사항)", example = "DELIVERED") @RequestParam(name = "shipmentStatus", required = false) ShipmentStatus shipmentStatus,
            @Parameter(description = "정산 상태 필터 (선택 사항)", example = "COMPLETED") @RequestParam(name = "settlementStatus", required = false) SettlementStatus settlementStatus
    ) {
        Long driverId = Long.valueOf(authentication.getPrincipal().toString());
        List<DriverSettlementListResponse> responses =
                shipmentService.getDriverSettlementList(driverId, year, month, shipmentStatus, settlementStatus);
        return ResponseEntity.ok(responses);
    }

    /**
     * 화주의 정산 요약 정보를 조회합니다.
     *
     * @param authentication 현재 인증된 사용자의 정보 (화주 ID 추출)
     * @return 화주 정산 요약 정보와 HTTP 200 OK 응답
     */
    @Operation(summary = "화주 정산 요약 조회", description = "인증된 화주의 정산 요약 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "정산 요약 조회 성공",
            content = @Content(schema = @Schema(implementation = ShipperSettlementSummaryResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/shipper/summary")
    public ResponseEntity<ShipperSettlementSummaryResponse> getShipperSettlementSummary(Authentication authentication) {
        Long shipperId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(shipmentService.getShipperSettlementSummary(shipperId));
    }

    /**
     * 운송 기사의 정산 요약 정보를 조회합니다.
     *
     * @param authentication 현재 인증된 사용자의 정보 (운송 기사 ID 추출)
     * @return 운송 기사 정산 요약 정보와 HTTP 200 OK 응답
     */
    @Operation(summary = "운송 기사 정산 요약 조회", description = "인증된 운송 기사의 정산 요약 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "정산 요약 조회 성공",
            content = @Content(schema = @Schema(implementation = DriverSettlementSummaryResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/driver/summary")
    public ResponseEntity<DriverSettlementSummaryResponse> getDriverSettlementSummary(Authentication authentication) {
        Long driverId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(shipmentService.getDriverSettlementSummary(driverId));
    }

    /**
     * 정산 완료 배송 상세 조회
     *
     * @param authentication 인증된 사용자(화주 또는 운송 기사)
     * @param shipmentId 조회할 배송 ID
     * @return 정산 화면용 배송 상세 DTO (화물/하차 사진 URL 모두 포함)
     */
    @Operation(summary = "정산 완료 배송 상세 조회", description = "완료된 배송의 상세 정보를 조회합니다. 화물 사진과 하차 사진 URL을 모두 반환합니다.")
    @ApiResponse(responseCode = "200", description = "정산 배송 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = ShipmentDetailResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "조회 권한이 없습니다.")
    @ApiResponse(responseCode = "404", description = "배송을 찾을 수 없습니다.")
    @ApiResponse(responseCode = "409", description = "완료되지 않은 배송은 조회할 수 없습니다.")
    @GetMapping("/detail/{shipmentId}")
    public ResponseEntity<ShipmentDetailResponse> getSettlementShipmentDetail(
            Authentication authentication,
            @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        ShipmentDetailResponse response = shipmentService.getSettlementShipmentDetail(userId, shipmentId);
        return ResponseEntity.ok(response);
    }
}
