package com.tjoeun.boxmon.feature.admin.service;

import com.tjoeun.boxmon.feature.admin.dto.AdminAssignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminAssignedShipmentDetailResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminForceCancelRequest;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentDetailResponse;

import java.util.List;

public interface AdminShipmentService {
    List<AdminUnassignedShipmentBasicResponse> getUnassignedBasic(Long adminId);

    AdminUnassignedShipmentDetailResponse getUnassignedDetail(Long adminId, Long shipmentId);

    List<AdminAssignedShipmentBasicResponse> getAssignedBasic(Long adminId);

    AdminAssignedShipmentDetailResponse getAssignedDetail(Long adminId, Long shipmentId);

    void forceCancel(Long adminId, Long shipmentId, AdminForceCancelRequest request);
}
