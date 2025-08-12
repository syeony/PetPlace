package com.minjeok4go.petplace.care.controller;

import com.minjeok4go.petplace.care.dto.CareRequestDto;
import com.minjeok4go.petplace.care.dto.CareResponseDto;
import com.minjeok4go.petplace.care.dto.CareListResponseDto;
import com.minjeok4go.petplace.care.entity.Cares.CareCategory;
import com.minjeok4go.petplace.care.entity.Cares.CareStatus;
import com.minjeok4go.petplace.care.service.CareService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cares")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Care", description = "돌봄/산책 요청 관리 API")
public class CareController {

    private final CareService careService;

    /**
     * 돌봄/산책 요청 등록
     */
    @PostMapping
    @Operation(summary = "돌봄/산책 요청 등록", description = "새로운 돌봄/산책 요청을 등록합니다.")
    public ApiResponse<CareResponseDto> createCare(
            @RequestBody @Valid CareRequestDto request,
            Authentication authentication) {

        log.info("돌봄/산책 요청 등록 - 사용자: {}, 카테고리: {}",
                authentication.getName(), request.getCategory());

        Long userId = getUserIdFromAuthentication(authentication);
        CareResponseDto response = careService.createCare(userId, request);

        return ApiResponse.success("돌봄/산책 요청이 성공적으로 등록되었습니다.", response);
    }

    /**
     * 돌봄/산책 요청 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "돌봄/산책 요청 상세 조회", description = "특정 돌봄/산책 요청의 상세 정보를 조회합니다.")
    public ApiResponse<CareResponseDto> getCare(
            @Parameter(description = "돌봄/산책 요청 ID") @PathVariable Long id) {

        CareResponseDto response = careService.getCare(id);
        return ApiResponse.success(response);
    }

    /**
     * 돌봄/산책 요청 수정
     */
    @PutMapping("/{id}")
    @Operation(summary = "돌봄/산책 요청 수정", description = "돌봄/산책 요청 내용을 수정합니다.")
    public ApiResponse<CareResponseDto> updateCare(
            @Parameter(description = "돌봄/산책 요청 ID") @PathVariable Long id,
            @RequestBody @Valid CareRequestDto request,
            Authentication authentication) {

        log.info("돌봄/산책 요청 수정 - ID: {}, 사용자: {}", id, authentication.getName());

        Long userId = getUserIdFromAuthentication(authentication);
        CareResponseDto response = careService.updateCare(id, userId, request);

        return ApiResponse.success("돌봄/산책 요청이 성공적으로 수정되었습니다.", response);
    }

    /**
     * 돌봄/산책 요청 삭제
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "돌봄/산책 요청 삭제", description = "돌봄/산책 요청을 삭제합니다.")
    public ApiResponse<Void> deleteCare(
            @Parameter(description = "돌봄/산책 요청 ID") @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        careService.deleteCare(id, userId);

        return ApiResponse.success("돌봄/산책 요청이 삭제되었습니다.", null);
    }

    /**
     * 돌봄/산책 요청 상태 변경
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "돌봄/산책 요청 상태 변경", description = "돌봄/산책 요청의 상태를 변경합니다.")
    public ApiResponse<CareResponseDto> updateCareStatus(
            @Parameter(description = "돌봄/산책 요청 ID") @PathVariable Long id,
            @Parameter(description = "변경할 상태") @RequestParam CareStatus status,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        CareResponseDto response = careService.updateCareStatus(id, userId, status);

        String message = getStatusChangeMessage(status);
        return ApiResponse.success(message, response);
    }

    /**
     * 지역별 돌봄/산책 요청 목록 조회
     */
    @GetMapping
    @Operation(summary = "지역별 돌봄/산책 요청 목록 조회", description = "특정 지역의 돌봄/산책 요청 목록을 조회합니다.")
    public ApiResponse<Page<CareListResponseDto>> getCaresByRegion(
            @Parameter(description = "지역 ID", example = "1100000000") @RequestParam Long regionId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<CareListResponseDto> response = careService.getCaresByRegion(regionId, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 카테고리별 돌봄/산책 요청 목록 조회
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 돌봄/산책 요청 목록 조회", description = "특정 카테고리의 돌봄/산책 요청 목록을 조회합니다.")
    public ApiResponse<Page<CareListResponseDto>> getCaresByCategory(
            @Parameter(description = "지역 ID") @RequestParam Long regionId,
            @Parameter(description = "카테고리") @PathVariable CareCategory category,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<CareListResponseDto> response = careService.getCaresByCategory(regionId, category, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 상태별 돌봄/산책 요청 목록 조회
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "상태별 돌봄/산책 요청 목록 조회", description = "특정 상태의 돌봄/산책 요청 목록을 조회합니다.")
    public ApiResponse<Page<CareListResponseDto>> getCaresByStatus(
            @Parameter(description = "지역 ID") @RequestParam Long regionId,
            @Parameter(description = "상태") @PathVariable CareStatus status,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<CareListResponseDto> response = careService.getCaresByStatus(regionId, status, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 내 돌봄/산책 요청 목록 조회
     */
    @GetMapping("/my")
    @Operation(summary = "내 돌봄/산책 요청 목록 조회", description = "로그인한 사용자의 돌봄/산책 요청 목록을 조회합니다.")
    public ApiResponse<Page<CareListResponseDto>> getMyCares(
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {

        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Long userId = getUserIdFromAuthentication(authentication);
        Page<CareListResponseDto> response = careService.getCaresByUser(userId, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 키워드 검색
     */
    @GetMapping("/search")
    @Operation(summary = "돌봄/산책 요청 검색", description = "키워드로 돌봄/산책 요청을 검색합니다.")
    public ApiResponse<Page<CareListResponseDto>> searchCares(
            @Parameter(description = "지역 ID") @RequestParam Long regionId,
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<CareListResponseDto> response = careService.searchCares(regionId, keyword, pageable);
        return ApiResponse.success(response);
    }

    /**
     * 복합 조건 검색
     */
    @GetMapping("/search/advanced")
    @Operation(summary = "돌봄/산책 요청 고급 검색", description = "여러 조건을 조합하여 돌봄/산책 요청을 검색합니다.")
    public ApiResponse<Page<CareListResponseDto>> searchCaresWithConditions(
            @Parameter(description = "지역 ID") @RequestParam Long regionId,
            @Parameter(description = "카테고리 (선택사항)") @RequestParam(required = false) CareCategory category,
            @Parameter(description = "상태 (선택사항)") @RequestParam(required = false) CareStatus status,
            @Parameter(description = "검색 키워드 (선택사항)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = createSortFromString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<CareListResponseDto> response = careService.searchCaresWithConditions(
                regionId, category, status, keyword, pageable);
        return ApiResponse.success(response);
    }

    // ===== 헬퍼 메서드들 =====

    /**
     * Authentication에서 사용자 ID 추출 - 기존 프로젝트 방식과 동일하지만 안전하게 처리
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        try {
            // JwtAuthenticationFilter에서 설정된 사용자 ID 추출
            String userId = authentication.getName();
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("인증된 사용자 ID를 찾을 수 없습니다.");
            }
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            log.error("사용자 ID 형식 오류: {}", authentication.getName());
            throw new IllegalArgumentException("잘못된 사용자 ID 형식입니다.");
        }
    }

    /**
     * 문자열에서 Sort 객체 생성
     */
    private Sort createSortFromString(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
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

        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    /**
     * 상태 변경 메시지 생성
     */
    private String getStatusChangeMessage(CareStatus status) {
        return switch (status) {
            case ACTIVE -> "요청이 활성화되었습니다.";
            case MATCHED -> "매칭이 완료되었습니다.";
            case COMPLETED -> "돌봄/산책이 완료되었습니다.";
            case CANCELLED -> "요청이 취소되었습니다.";
        };
    }
}