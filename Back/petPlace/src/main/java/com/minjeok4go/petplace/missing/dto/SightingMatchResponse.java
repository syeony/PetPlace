package com.minjeok4go.petplace.missing.dto;

import com.minjeok4go.petplace.missing.entity.SightingMatch;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SightingMatchResponse {
    private Long id;
    private Long sightingId;
    private Long missingReportId;
    private BigDecimal score;
    private String status;
    private LocalDateTime createdAt;
    private SightingResponse sighting;
    private MissingReportResponse missingReport;

    public static SightingMatchResponse from(SightingMatch match,
                                             SightingResponse sighting,
                                             MissingReportResponse missingReport) {
        return SightingMatchResponse.builder()
                .id(match.getId())
                .sightingId(match.getSighting().getId())
                .missingReportId(match.getMissingReport().getId())
                .score(match.getScore())
                .status(match.getStatus().name())
                .createdAt(match.getCreatedAt())
                .sighting(sighting)
                .missingReport(missingReport)
                .build();
    }
}