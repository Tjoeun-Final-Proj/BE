package com.tjoeun.boxmon.feature.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationLogRequest {

    @NotNull(message = "운송건 ID는 필수입니다.")
    private Long shipmentId;

    @NotBlank(message = "위치 데이터 청크는 필수입니다.")
    private String locationChunk; // 프론트에서 전송하는 JSON 문자열 (10분 단위 청크)
}
