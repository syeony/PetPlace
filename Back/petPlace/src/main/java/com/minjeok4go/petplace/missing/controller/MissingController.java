package com.minjeok4go.petplace.missing.controller;

import com.minjeok4go.petplace.missing.dto.MissingReportCreateRequest;
import com.minjeok4go.petplace.missing.dto.SightingCreateRequest;
import com.minjeok4go.petplace.missing.dto.MissingReportResponse;
import com.minjeok4go.petplace.missing.dto.SightingResponse;
import com.minjeok4go.petplace.missing.dto.SightingMatchResponse;
import com.minjeok4go.petplace.missing.entity.MissingReport;
import com.minjeok4go.petplace.missing.entity.SightingMatch;
import com.minjeok4go.petplace.missing.service.MissingService;
import com.minjeok4go.petplace.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Missing", description = "실종 동물 신고 및 목격 제보 API")
public class MissingController {

    private final MissingService missingService;
    /**
     * 실종 신고 등록 
     */
    @PostMapping("/reports")
    @Operation(summary = "실종 신고 등록", description = "반려동물 실종 신고를 등록합니다.")
    public ApiResponse<MissingReportResponse> createMissingReport(
            @RequestBody @Valid MissingReportCreateRequest request,
            Authentication authentication) {

        log.info("실종 신고 등록 요청 - 사용자: {}, 반려동물: {}",
                authentication.getName(), request.getPetId());

        Long userId = getUserIdFromAuthentication(authentication);
        MissingReportResponse response = missingService.createMissingReport(userId, request);

        return ApiResponse.success("실종 신고가 성공적으로 등록되었습니다.", response);
    }

    /**
     * 목격 제보 등록
     */
    @PostMapping("/sightings")
    @Operation(summary = "목격 제보 등록", description = "실종 동물 목격 제보를 등록합니다. AI가 자동으로 품종을 분석하고 매칭을 수행합니다.")
    public ApiResponse<SightingResponse> createSighting(
            @RequestBody @Valid SightingCreateRequest request,
            Authentication authentication) {

        log.info("목격 제보 등록 요청 - 사용자: {}", authentication.getName());

        Long userId = getUserIdFromAuthentication(authentication);
        SightingResponse response = missingService.createSighting(userId, request);

        return ApiResponse.success("목격 제보가 성공적으로 등록되었습니다. AI 분석 후 자동 매칭 결과를 알려드리겠습니다.", response);
    }

    /**
     * 실종 신고 상세 조회
     */
    @GetMapping("/reports/{id}")
    @Operation(summary = "실종 신고 상세 조회", description = "특정 실종 신고의 상세 정보를 조회합니다.")
    public ApiResponse<MissingReportResponse> getMissingReport(
            @Parameter(description = "실종 신고 ID") @PathVariable Long id) {

        MissingReportResponse response = missingService.getMissingReport(id);
        return ApiResponse.success(response);
    }

    /**
     * 목격 제보 상세 조회
     */
    @GetMapping("/sightings/{id}")
    @Operation(summary = "목격 제보 상세 조회", description = "특정 목격 제보의 상세 정보를 조회합니다.")
    public ApiResponse<SightingResponse> getSighting(
            @Parameter(description = "목격 제보 ID") @PathVariable Long id) {

        SightingResponse response = missingService.getSighting(id);
        return ApiResponse.success(response);
    }

    /**
     * 실종 신고 수정
     */
    @PutMapping("/reports/{id}")
    @Operation(summary = "실종 신고 수정", description = "실종 신고 내용을 수정합니다.")
    public ApiResponse<MissingReportResponse> updateMissingReport(
            @Parameter(description = "실종 신고 ID") @PathVariable Long id,
            @RequestBody @Valid MissingReportCreateRequest request,
            Authentication authentication) {

        log.info("실종 신고 수정 요청 - ID: {}, 사용자: {}", id, authentication.getName());

        Long userId = getUserIdFromAuthentication(authentication);
        MissingReportResponse response = missingService.updateMissingReport(id, userId, request);

        return ApiResponse.success("실종 신고가 성공적으로 수정되었습니다.", response);
    }

    /**
     * 목격 제보 수정
     */
    @PutMapping("/sightings/{id}")
    @Operation(summary = "목격 제보 수정", description = "목격 제보 내용을 수정합니다.")
    public ApiResponse<SightingResponse> updateSighting(
            @Parameter(description = "목격 제보 ID") @PathVariable Long id,
            @RequestBody @Valid SightingCreateRequest request,
            Authentication authentication) {

        log.info("목격 제보 수정 요청 - ID: {}, 사용자: {}", id, authentication.getName());

        Long userId = getUserIdFromAuthentication(authentication);
        SightingResponse response = missingService.updateSighting(id, userId, request);

        return ApiResponse.success("목격 제보가 성공적으로 수정되었습니다.", response);
    }

    /**
     * 지역별 실종 신고 목록 조회
     */
    @GetMapping("/reports")
    @Operation(summary = "지역별 실종 신고 목록 조회", description = "특정 지역의 실종 신고 목록을 조회합니다.")
    public ApiResponse<Page<MissingReportResponse>> getMissingReportsByRegion(
            @Parameter(description = "지역 ID", example = "1100000000") @RequestParam Long regionId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (예: createdAt,desc)", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        // Sort 객체 생성
        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<MissingReportResponse> response = missingService.getMissingReportsByRegion(regionId, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 지역별 목격 제보 목록 조회
     */
    @GetMapping("/sightings")
    @Operation(summary = "지역별 목격 제보 목록 조회", description = "특정 지역의 목격 제보 목록을 조회합니다.")
    public ApiResponse<Page<SightingResponse>> getSightingsByRegion(
            @Parameter(description = "지역 ID", example = "1100000000") @RequestParam Long regionId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (예: createdAt,desc)", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        // Sort 객체 생성
        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<SightingResponse> response = missingService.getSightingsByRegion(regionId, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 내 실종 신고 목록 조회
     */
    @GetMapping("/my-reports")
    @Operation(summary = "내 실종 신고 목록 조회", description = "로그인한 사용자의 실종 신고 목록을 조회합니다.")
    public ApiResponse<Page<MissingReportResponse>> getMyMissingReports(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (예: createdAt,desc)", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {

        // Sort 객체 생성
        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Long userId = getUserIdFromAuthentication(authentication);
        Page<MissingReportResponse> response = missingService.getMissingReportsByUser(userId, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 실종 신고의 매칭 결과 조회
     */
    @GetMapping("/reports/{id}/matches")
    @Operation(summary = "실종 신고 매칭 결과 조회", description = "특정 실종 신고에 대한 AI 매칭 결과를 조회합니다.")
    public ApiResponse<Page<SightingMatchResponse>> getMatchesForMissingReport(
            @Parameter(description = "실종 신고 ID") @PathVariable Long id,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (예: score,desc)", example = "score,desc") @RequestParam(defaultValue = "score,desc") String sort) {

        // Sort 객체 생성
        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<SightingMatchResponse> response = missingService.getMatchesForMissingReport(id, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 내 매칭 알림 조회
     */
    @GetMapping("/my-matches")
    @Operation(summary = "내 매칭 알림 조회", description = "내 실종 신고에 대한 매칭 알림을 조회합니다.")
    public ApiResponse<Page<SightingMatchResponse>> getMyPendingMatches(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (예: createdAt,desc)", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {

        // Sort 객체 생성
        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Long userId = getUserIdFromAuthentication(authentication);
        Page<SightingMatchResponse> response = missingService.getPendingMatchesForUser(userId, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 실종 신고 상태 변경
     */
    @PatchMapping("/reports/{id}/status")
    @Operation(summary = "실종 신고 상태 변경", description = "실종 신고의 상태를 변경합니다 (찾음/취소).")
    public ApiResponse<MissingReportResponse> updateMissingReportStatus(
            @Parameter(description = "실종 신고 ID") @PathVariable Long id,
            @Parameter(description = "변경할 상태") @RequestParam MissingReport.MissingStatus status,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        MissingReportResponse response = missingService.updateMissingReportStatus(id, userId, status);

        String message = status == MissingReport.MissingStatus.FOUND ?
                "실종 동물을 찾았다고 신고되었습니다!" : "실종 신고가 취소되었습니다.";

        return ApiResponse.success(message, response);
    }

    /**
     * 매칭 상태 변경
     */
    @PatchMapping("/matches/{id}/status")
    @Operation(summary = "매칭 상태 변경", description = "AI 매칭 결과에 대한 상태를 변경합니다 (확인/거부).")
    public ApiResponse<SightingMatchResponse> updateMatchStatus(
            @Parameter(description = "매칭 ID") @PathVariable Long id,
            @Parameter(description = "변경할 상태") @RequestParam SightingMatch.MatchStatus status,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        SightingMatchResponse response = missingService.updateMatchStatus(id, userId, status);

        String message = status == SightingMatch.MatchStatus.CONFIRMED ?
                "매칭을 확인했습니다!" : "매칭을 거부했습니다.";

        return ApiResponse.success(message, response);
    }

    /**
     * 실종 신고 삭제
     */
    @DeleteMapping("/reports/{id}")
    @Operation(summary = "실종 신고 삭제", description = "실종 신고를 삭제합니다.")
    public ApiResponse<Void> deleteMissingReport(
            @Parameter(description = "실종 신고 ID") @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        missingService.deleteMissingReport(id, userId);

        return ApiResponse.success("실종 신고가 삭제되었습니다.", null);
    }

    /**
     * 목격 제보 삭제
     */
    @DeleteMapping("/sightings/{id}")
    @Operation(summary = "목격 제보 삭제", description = "목격 제보를 삭제합니다.")
    public ApiResponse<Void> deleteSighting(
            @Parameter(description = "목격 제보 ID") @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        missingService.deleteSighting(id, userId);

        return ApiResponse.success("목격 제보가 삭제되었습니다.", null);
    }

    /**
     * Authentication에서 사용자 ID 추출
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        return Long.valueOf(authentication.getName()); // 임시 구현
    }

    /**
     * 문자열에서 Sort 객체 생성하는 헬퍼 메서드
     */
    private Sort createSortFromString(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt"); // 기본값
        }

        String[] sortParams = sort.split(",");
        if (sortParams.length == 2) {
            String property = sortParams[0].trim();
            String direction = sortParams[1].trim();

            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            return Sort.by(sortDirection, property);
        }

        // 잘못된 형식일 경우 기본값 반환
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}