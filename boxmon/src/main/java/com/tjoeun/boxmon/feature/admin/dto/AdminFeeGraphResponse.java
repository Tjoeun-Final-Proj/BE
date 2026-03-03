package com.tjoeun.boxmon.feature.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminFeeGraphResponse {
    private String fromDate;
    private String toDate;
    private String unit;
    private List<AdminFeeGraphPointResponse> points;
}

