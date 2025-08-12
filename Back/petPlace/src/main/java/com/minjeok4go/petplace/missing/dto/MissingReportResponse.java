package com.minjeok4go.petplace.missing.dto;

import com.minjeok4go.petplace.missing.entity.MissingReport;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MissingReportResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private String userImg;
    private Long petId;
    private String petName;
    private String petBreed;
    private String petImg;
    private Long regionId;
    private String regionName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String content;
    private String status;
    private LocalDateTime missingAt;
    private LocalDateTime createdAt;
    private List<ImageResponse> images;

    public static MissingReportResponse from(MissingReport missingReport, List<ImageResponse> images) {
        return MissingReportResponse.builder()
                .id(missingReport.getId())
                .userId(missingReport.getUser().getId())
                .userNickname(missingReport.getUser().getNickname())
                .userImg(missingReport.getUser().getUserImgSrc())
                .petId(missingReport.getPet().getId())
                .petName(missingReport.getPet().getName())
                .petBreed(missingReport.getPet().getBreed().name())
                .petImg(missingReport.getPet().getImgSrc())
                .regionId(missingReport.getRegion().getId())
                .regionName(missingReport.getRegion().getName())
                .address(missingReport.getAddress())
                .latitude(missingReport.getLatitude())
                .longitude(missingReport.getLongitude())
                .content(missingReport.getContent())
                .status(missingReport.getStatus().name())
                .missingAt(missingReport.getMissingAt())
                .createdAt(missingReport.getCreatedAt())
                .images(images)
                .build();
    }
}