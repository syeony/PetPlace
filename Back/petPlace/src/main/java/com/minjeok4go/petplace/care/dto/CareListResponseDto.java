package com.minjeok4go.petplace.care.dto;

import com.minjeok4go.petplace.care.entity.Cares;
import com.minjeok4go.petplace.care.entity.Cares.CareCategory;
import com.minjeok4go.petplace.care.entity.Cares.CareStatus;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CareListResponseDto {
    private Long id;
    private String title;
    private String content;

    // 사용자 정보 (간소화)
    private Long userId;
    private String userNickname;
    private String userImg;

    // 반려동물 정보 (간소화)
    private Long petId;
    private String petName;
    private String petImg;
    private String animalType;

    // 지역 정보
    private String regionName;

    // 카테고리 및 상태
    private CareCategory category;
    private String categoryDescription;
    private CareStatus status;

    // 날짜/시간 정보
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;

    // 메타 정보
    private Integer views;
    private LocalDateTime createdAt;

    // 이미지 정보
    private List<ImageResponse> images;

    public static CareListResponseDto from(Cares care, List<ImageResponse> images) {
        return CareListResponseDto.builder()
                .id(care.getId())
                .title(care.getTitle())
                .content(care.getContent())
                .userId(care.getUser().getId())
                .userNickname(care.getUser().getNickname())
                .userImg(care.getUser().getUserImgSrc())
                .petId(care.getPet().getId())
                .petName(care.getPet().getName())
                .petImg(care.getPet().getImgSrc())
                .animalType(care.getPet().getAnimal().getDisplayName())
                .regionName(care.getRegion().getName())
                .category(care.getCategory())
                .categoryDescription(care.getCategory().getDescription())
                .status(care.getStatus())
                .startDatetime(care.getStartDatetime())
                .endDatetime(care.getEndDatetime())
                .views(care.getViews())
                .createdAt(care.getCreatedAt())
                .images(images)
                .build();
    }
}
