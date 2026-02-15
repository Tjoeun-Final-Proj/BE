package com.tjoeun.boxmon.feature.shipment.controller;

import com.tjoeun.boxmon.feature.shipment.dto.ShipmentCreateRequest;
import com.tjoeun.boxmon.feature.shipment.dto.ShipmentDetailResponse;
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

/**
 * 배송 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 배송 생성 및 상세 정보 조회를 담당합니다.
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
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentDetailResponse> getShipmentDetail(
            @Parameter(description = "배송 ID", example = "1") @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        // Service computes ETA/distance when current driver location exists.
        ShipmentDetailResponse response = shipmentService.getShipmentDetail(shipmentId);
        return ResponseEntity.ok(response);
    }
}