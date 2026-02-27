package com.tjoeun.boxmon.feature.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class AdminFeeGraphPointResponse {
    private String date;
    private BigDecimal feeRate;
    private boolean changed;
}

