package com.tjoeun.boxmon.feature.shipment.controller;

import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
import com.tjoeun.boxmon.feature.shipment.dto.UnassignedShipmentResponse;
import com.tjoeun.boxmon.feature.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 배송 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 배송 생성, 배차 수락, 상세 조회 및 미배차 목록 조회를 담당합니다.
 */
@Tag(name = "배송", description = "배송 생성 및 조회 관련 API")
@RestController
@RequestMapping("/api/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    /**
     * 새로운 배송을 생성합니다.
     *
     * @param authentication 현재 인증된 화주의 정보 (화주 ID 추출)
     * @param request 배송 생성 요청 데이터
     */
    @Operation(summary = "배송 생성", description = "새로운 배송을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "배송 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createShipment(
            Authentication authentication,
            @Parameter(description = "배송 생성 요청 본문", required = true) @RequestBody @Valid ShipmentCreateRequest request
    ) {
        Long shipperId = Long.valueOf(authentication.getPrincipal().toString());
        shipmentService.createShipment(shipperId, request);
    }

    /**
     * 배차 상태의 배송을 지정 기사에게 배차 수락 처리합니다.
     *
     * @param authentication 현재 인증된 운송 기사 정보 (기사 ID 추출)
     * @param shipmentId 배차 대상 배송 ID
     */
    @Operation(summary = "배차 수락", description = "배차 대기 건을 본인으로 배차 처리합니다.")
    @ApiResponse(responseCode = "204", description = "배차 수락 처리 완료")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "배송을 찾을 수 없음")
    @ApiResponse(responseCode = "409", description = "배차 상태가 유효하지 않거나 이미 배차된 배송")
    @PostMapping("/{shipmentId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptShipment(
            Authentication authentication,
            @Parameter(description = "배송 ID", example = "1") @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        Long driverId = Long.valueOf(authentication.getPrincipal().toString());
        shipmentService.acceptShipment(driverId, shipmentId);
    }

    /**
     * 특정 배송의 상세 정보를 조회합니다.
     * 화주/운송 기사 상세 화면에 필요한 정보를 제공합니다.
     *
     * @param shipmentId 조회할 배송의 고유 ID
     * @return 배송 상세 정보와 HTTP 200 OK 응답
     */
    @Operation(summary = "배송 상세 정보 조회", description = "특정 배송의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "배송 상세 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = ShipmentDetailResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "배송을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/detail/{shipmentId}")
    public ResponseEntity<ShipmentDetailResponse> getShipmentDetail(
            @Parameter(description = "배송 ID", example = "1") @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        // 서비스에서 운송 기사 현재 위치 존재 여부에 따라 ETA/거리 계산 분기를 처리합니다.
        ShipmentDetailResponse response = shipmentService.getShipmentDetail(shipmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 배차 수락용 화물 상세 정보를 조회합니다. (ETA/거리 계산 없음)
     *
     * @param shipmentId 조회할 배송의 고유 ID
     * @return 배송 상세 응답 DTO
     */
    @Operation(summary = "배차 수락용 화물 상세 정보 조회", description = "ETA/거리 계산 없이 화물 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "배송 상세 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = ShipmentDetailResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "배송을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/accept-detail/{shipmentId}")
    public ResponseEntity<ShipmentDetailResponse> getShipmentAcceptDetail(
            @Parameter(description = "배송 ID", example = "1") @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        ShipmentDetailResponse response = shipmentService.getShipmentAcceptDetail(shipmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 미배차 화물 목록을 조회합니다.
     *
     * @return 미배차 화물 목록과 HTTP 200 OK 응답
     */
    @Operation(summary = "미배차 화물 목록 조회", description = "배차되지 않은 모든 화물 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "미배차 화물 목록 조회 성공")
    @GetMapping("/unassigned")
    public ResponseEntity<List<UnassignedShipmentResponse>> getUnassignedShipments() {
        List<UnassignedShipmentResponse> response = shipmentService.getUnassignedShipments();
        return ResponseEntity.ok(response);
    }
}
