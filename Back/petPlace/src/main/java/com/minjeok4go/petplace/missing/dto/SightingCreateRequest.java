package com.minjeok4go.petplace.missing.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SightingCreateRequest {

    @NotNull(message = "지역 ID는 필수입니다")
    private Long regionId;

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 300, message = "주소는 300자를 초과할 수 없습니다")
    private String address;

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
    private BigDecimal latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
    private BigDecimal longitude;

    @NotBlank(message = "목격 내용은 필수입니다")
    @Size(max = 2000, message = "목격 내용은 2000자를 초과할 수 없습니다")
    private String content;

    @NotNull(message = "목격 일시는 필수입니다")
    private LocalDateTime sightedAt;

    // 목격된 동물 사진 URL 리스트 (최소 1개, 최대 5개)
    @NotEmpty(message = "목격 사진은 최소 1개 이상 필수입니다")
    @Size(max = 5, message = "이미지는 최대 5개까지 등록 가능합니다")
    private List<MissingImageRequest> images;

    // ↓↓↓ 선택/권장: 온디바이스 결과를 그대로 받기
    private String breedEng;
    private String species;   // "dog" | "cat" (optional, 없으면 서버 기본)
    private Integer xmin;
    private Integer ymin;
    private Integer xmax;
    private Integer ymax;
    private Double wFace;     // optional
}