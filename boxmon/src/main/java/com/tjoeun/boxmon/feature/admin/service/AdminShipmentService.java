package com.tjoeun.boxmon.feature.admin.service;

import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentBasicResponse;
import com.tjoeun.boxmon.feature.admin.dto.AdminUnassignedShipmentDetailResponse;

import java.util.List;

public interface AdminShipmentService {
    List<AdminUnassignedShipmentBasicResponse> getUnassignedBasic(Long adminId);

    AdminUnassignedShipmentDetailResponse getUnassignedDetail(Long adminId, Long shipmentId);
}
