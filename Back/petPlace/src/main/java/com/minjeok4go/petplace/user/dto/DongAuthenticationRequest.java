package com.minjeok4go.petplace.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "동네 인증 요청 DTO")
public class DongAuthenticationRequest {

    @Schema(description = "위도 (WGS84)", example = "37.5665")
    private Double lat;

    @Schema(description = "경도 (WGS84)", example = "126.9780")
    private Double lon;
}
