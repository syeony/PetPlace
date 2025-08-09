package com.minjeok4go.petplace.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "호텔 예약 생성 요청 DTO")
@Getter @Setter
public class ReservationCreateRequest {


    @Schema(description = "예약할 반려동물의 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "반려동물 ID는 필수입니다.")
    private Long petId;

    @Schema(description = "예약할 호텔의 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "호텔 ID는 필수입니다.")
    private Long hotelId;

    @Schema(description = "예약할 날짜 목록 (YYYY-MM-DD 형식)", example = "[\"2024-08-15\", \"2024-08-16\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "예약할 날짜를 최소 1개 이상 선택해야 합니다.")
    private List<LocalDate> selectedDates;

    @Schema(description = "특별 요청 사항", example = "창가 쪽 방으로 부탁드려요.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String specialRequests;


    /**
     * 선택된 날짜들이 연속된 날짜인지 확인
     */
    public boolean isConsecutiveDates() {
        if (selectedDates == null || selectedDates.size() <= 1) {
            return true;
        }

        List<LocalDate> sortedDates = selectedDates.stream()
                .sorted()
                .toList();

        for (int i = 1; i < sortedDates.size(); i++) {
            if (!sortedDates.get(i).equals(sortedDates.get(i-1).plusDays(1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 체크인 날짜 (선택된 날짜 중 가장 이른 날짜)
     */
    public LocalDate getCheckInDate() {
        return selectedDates.stream()
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * 체크아웃 날짜 (선택된 날짜 중 가장 늦은 날짜)
     */
    public LocalDate getCheckOutDate() {
        return selectedDates.stream()
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * 총 숙박일수
     */
    public int getTotalDays() {
        return selectedDates.size();
    }
}