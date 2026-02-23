package com.tjoeun.boxmon.feature.admin.controller;

import com.tjoeun.boxmon.feature.admin.dto.AdminAssignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminAssignedShipmentDetailResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentDetailResponse;
import com.tjoeun.boxmon.feature.admin.service.AdminShipmentService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 관리자용 화물 조회 API를 처리하는 컨트롤러 클래스입니다.
 * 미배차/배차 화물의 basic 목록 조회와 detail 단건 조회를 담당합니다.
 */
@Tag(name = "관리자 화물 조회", description = "관리자 웹의 화물 조회 관련 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminShipmentQueryController {

    private final AdminShipmentService adminShipmentService;

    /**
     * 미배차(REQUESTED) 화물 basic 목록을 조회합니다.
     *
     * @param authentication 현재 인증된 관리자 정보 (관리자 ID 추출)
     * @return 미배차 화물 basic 목록과 HTTP 200 OK 응답
     */
    @Operation(summary = "관리자 미배차 화물 basic 조회", description = "관리자 화면에서 미배차 화물의 기본 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "미배차 화물 basic 조회 성공",
            content = @Content(schema = @Schema(implementation = AdminUnassignedShipmentBasicResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    @GetMapping("/unassigned/basic")
    public ResponseEntity<List<AdminUnassignedShipmentBasicResponse>> getUnassignedBasic(
            Authentication authentication
    ) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(adminShipmentService.getUnassignedBasic(adminId));
    }

    /**
     * 미배차(REQUESTED) 화물 detail 정보를 조회합니다.
     *
     * @param authentication 현재 인증된 관리자 정보 (관리자 ID 추출)
     * @param shipmentId 조회할 화물 ID
     * @return 미배차 화물 detail 정보와 HTTP 200 OK 응답
     */
    @Operation(summary = "관리자 미배차 화물 detail 조회", description = "관리자 화면에서 미배차 화물의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "미배차 화물 detail 조회 성공",
            content = @Content(schema = @Schema(implementation = AdminUnassignedShipmentDetailResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    @ApiResponse(responseCode = "404", description = "미배차 화물을 찾을 수 없음")
    @GetMapping("/unassigned/detail/{shipmentId}")
    public ResponseEntity<AdminUnassignedShipmentDetailResponse> getUnassignedDetail(
            Authentication authentication,
            @Parameter(description = "화물 ID", example = "1") @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(adminShipmentService.getUnassignedDetail(adminId, shipmentId));
    }

    /**
     * 배차(ASSIGNED) 화물 basic 목록을 조회합니다.
     *
     * @param authentication 현재 인증된 관리자 정보 (관리자 ID 추출)
     * @return 배차 화물 basic 목록과 HTTP 200 OK 응답
     */
    @Operation(summary = "관리자 배차 화물 basic 조회", description = "관리자 화면에서 배차 완료 화물의 기본 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "배차 화물 basic 조회 성공",
            content = @Content(schema = @Schema(implementation = AdminAssignedShipmentBasicResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    @GetMapping("/assigned/basic")
    public ResponseEntity<List<AdminAssignedShipmentBasicResponse>> getAssignedBasic(
            Authentication authentication
    ) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(adminShipmentService.getAssignedBasic(adminId));
    }

    /**
     * 배차(ASSIGNED) 화물 detail 정보를 조회합니다.
     *
     * @param authentication 현재 인증된 관리자 정보 (관리자 ID 추출)
     * @param shipmentId 조회할 화물 ID
     * @return 배차 화물 detail 정보와 HTTP 200 OK 응답
     */
    @Operation(summary = "관리자 배차 화물 detail 조회", description = "관리자 화면에서 배차 완료 화물의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "배차 화물 detail 조회 성공",
            content = @Content(schema = @Schema(implementation = AdminAssignedShipmentDetailResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    @ApiResponse(responseCode = "404", description = "배차 화물을 찾을 수 없음")
    @GetMapping("/assigned/detail/{shipmentId}")
    public ResponseEntity<AdminAssignedShipmentDetailResponse> getAssignedDetail(
            Authentication authentication,
            @Parameter(description = "화물 ID", example = "1") @PathVariable(name = "shipmentId") Long shipmentId
    ) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(adminShipmentService.getAssignedDetail(adminId, shipmentId));
    }
}
