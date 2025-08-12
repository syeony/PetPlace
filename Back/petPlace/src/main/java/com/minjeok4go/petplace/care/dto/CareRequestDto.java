package com.minjeok4go.petplace.care.dto;

import com.minjeok4go.petplace.care.entity.Cares.CareCategory;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class CareRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자를 초과할 수 없습니다")
    private String content;

    @NotNull(message = "반려동물 ID는 필수입니다")
    private Long petId;

    @NotNull(message = "지역 ID는 필수입니다")
    private Long regionId;

    @NotNull(message = "카테고리는 필수입니다")
    private CareCategory category;

    // 공통 - 시작 날짜
    @NotNull(message = "시작 날짜는 필수입니다")
    private LocalDate startDate;

    // 돌봄일 경우 - 종료 날짜 (산책일 경우 null)
    private LocalDate endDate;

    // 산책일 경우 - 시작 시간 (돌봄일 경우 null)
    private LocalTime startTime;

    // 산책일 경우 - 종료 시간 (돌봄일 경우 null)
    private LocalTime endTime;

    /**
     * 유효성 검증 메서드
     */
    public void validate() {
        if (isWalkCategory()) {
            validateWalkRequest();
        } else {
            validateCareRequest();
        }
    }

    private boolean isWalkCategory() {
        return category == CareCategory.WALK_WANT || category == CareCategory.WALK_REQ;
    }

    private void validateWalkRequest() {
        if (startTime == null) {
            throw new IllegalArgumentException("산책 요청은 시작 시간이 필수입니다");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("산책 요청은 종료 시간이 필수입니다");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다");
        }
        if (endDate != null) {
            throw new IllegalArgumentException("산책 요청에는 종료 날짜를 설정할 수 없습니다");
        }
    }

    private void validateCareRequest() {
        if (endDate == null) {
            throw new IllegalArgumentException("돌봄 요청은 종료 날짜가 필수입니다");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다");
        }
        if (startTime != null || endTime != null) {
            throw new IllegalArgumentException("돌봄 요청에는 시간을 설정할 수 없습니다");
        }
    }
}
