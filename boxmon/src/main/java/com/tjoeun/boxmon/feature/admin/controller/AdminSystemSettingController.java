package com.tjoeun.boxmon.feature.admin.controller;

import com.tjoeun.boxmon.feature.admin.dto.AdminFeeChangeHistoryResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeGraphResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeSettingResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminFeeSettingUpdateRequest;
import com.tjoeun.boxmon.global.systemsetting.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "관리자 설정", description = "관리자 전역 설정 관련 API")
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSystemSettingController {

    private final SystemSettingService systemSettingService;

    @Operation(summary = "수수료율 설정 조회", description = "현재 전역 수수료율 설정을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    @GetMapping("/fee")
    public ResponseEntity<AdminFeeSettingResponse> getFeeSetting(Authentication authentication) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(systemSettingService.getFeeSetting(adminId));
    }

    @Operation(summary = "수수료율 설정 수정", description = "전역 수수료율 설정값을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 값")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    @PutMapping("/fee")
    public ResponseEntity<AdminFeeSettingResponse> updateFeeSetting(
            Authentication authentication,
            @Valid @RequestBody AdminFeeSettingUpdateRequest request
    ) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(systemSettingService.updateFeeSetting(adminId, request.getValue()));
    }

    @Operation(summary = "수수료율 변경 이력 조회", description = "수수료율 변경 이벤트 이력을 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    @GetMapping("/fee/history")
    public ResponseEntity<List<AdminFeeChangeHistoryResponse>> getFeeSettingHistory(Authentication authentication) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(systemSettingService.getFeeSettingHistory(adminId));
    }

    @Operation(summary = "2주 수수료율 그래프 조회", description = "최근 14일 수수료율 그래프 데이터를 일 단위로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    @GetMapping("/fee/graph/2weeks")
    public ResponseEntity<AdminFeeGraphResponse> getFeeRateGraphForLast2Weeks(Authentication authentication) {
        Long adminId = Long.valueOf(authentication.getPrincipal().toString());
        return ResponseEntity.ok(systemSettingService.getFeeRateGraphForLast2Weeks(adminId));
    }
}
