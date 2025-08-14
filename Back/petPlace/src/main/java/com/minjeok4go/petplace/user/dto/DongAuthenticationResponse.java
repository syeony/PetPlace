package com.minjeok4go.petplace.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "동네 인증 응답 DTO")
public class DongAuthenticationResponse {
    
    @Schema(description = "지역 ID", example = "11110101")
    private Long regionId;
    
    @Schema(description = "지역 이름", example = "종로1가동")
    private String regionName;
    
    public static DongAuthenticationResponse of(Long regionId, String regionName) {
        return DongAuthenticationResponse.builder()
                .regionId(regionId)
                .regionName(regionName)
                .build();
    }
}
