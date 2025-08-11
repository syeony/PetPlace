package com.minjeok4go.petplace.missing.dto;

import com.minjeok4go.petplace.missing.entity.Sighting;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SightingResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private String userImg;
    private Long regionId;
    private String regionName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String content;
    private String breed; // AI 예측 품종 추가
    private LocalDateTime sightedAt;
    private LocalDateTime createdAt;
    private List<ImageResponse> images;

    public static SightingResponse from(Sighting sighting, List<ImageResponse> images) {
        return SightingResponse.builder()
                .id(sighting.getId())
                .userId(sighting.getUser().getId())
                .userNickname(sighting.getUser().getNickname())
                .userImg(sighting.getUser().getUserImgSrc())
                .regionId(sighting.getRegion().getId())
                .regionName(sighting.getRegion().getName())
                .address(sighting.getAddress())
                .latitude(sighting.getLatitude())
                .longitude(sighting.getLongitude())
                .content(sighting.getContent())
                .breed(sighting.getBreed() != null ? sighting.getBreed().name() : null) // AI 예측 품종
                .sightedAt(sighting.getSightedAt())
                .createdAt(sighting.getCreatedAt())
                .images(images)
                .build();
    }
}