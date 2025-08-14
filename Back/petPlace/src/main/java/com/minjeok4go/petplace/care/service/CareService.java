package com.minjeok4go.petplace.care.service;

import com.minjeok4go.petplace.care.dto.CareImageRequest;
import com.minjeok4go.petplace.care.dto.CareRequestDto;
import com.minjeok4go.petplace.care.dto.CareResponseDto;
import com.minjeok4go.petplace.care.dto.CareListResponseDto;
import com.minjeok4go.petplace.care.entity.Cares;
import com.minjeok4go.petplace.care.entity.Cares.CareCategory;
import com.minjeok4go.petplace.care.entity.Cares.CareStatus;
import com.minjeok4go.petplace.care.repository.CareRepository;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.pet.repository.PetRepository;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.region.repository.RegionRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import com.minjeok4go.petplace.image.service.ImageService;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.common.constant.RefType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CareService {

    private final CareRepository careRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final RegionRepository regionRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository; // 이미지 업데이트용

    /**
     * 돌봄/산책 요청 등록
     */
    @Transactional
    public CareResponseDto createCare(Long userId, CareRequestDto requestDto) {
        log.info("돌봄/산책 요청 등록 시작 - 사용자: {}, 카테고리: {}", userId, requestDto.getCategory());

        // 요청 데이터 유효성 검증
        requestDto.validate();

        // 엔티티 조회
        User user = getUserById(userId);
        Pet pet = getPetById(requestDto.getPetId());
        Region region = getRegionById(requestDto.getRegionId());

        // 반려동물 소유자 확인
        validatePetOwnership(pet, userId);

        // 날짜/시간 변환
        LocalDateTime[] datetimes = convertToDatetimes(requestDto);
        LocalDateTime startDatetime = datetimes[0];
        LocalDateTime endDatetime = datetimes[1];

        // 과거 날짜 확인
        validateFutureDateTime(startDatetime);

        // 돌봄/산책 요청 생성
        Cares care = Cares.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .user(user)
                .pet(pet)
                .region(region)
                .category(requestDto.getCategory())
                .startDatetime(startDatetime)
                .endDatetime(endDatetime)
                .build();

        Cares savedCare = careRepository.save(care);

        // 이미지 저장 (기존 ImageService 활용)
        saveImages(requestDto.getImages(), savedCare.getId(), RefType.CARE);

        log.info("돌봄/산책 요청 등록 완료 - ID: {}", savedCare.getId());

        // 이미지 정보 포함해서 응답
        List<ImageResponse> images = imageService.getImages(RefType.CARE, savedCare.getId());
        return CareResponseDto.from(savedCare, images);
    }

    /**
     * 돌봄/산책 요청 상세 조회
     */
    @Transactional
    public CareResponseDto getCare(Long careId) {
        Cares care = getCareById(careId);

        // 조회수 증가
        care.increaseViews();

        // 이미지 정보 조회
        List<ImageResponse> images = imageService.getImages(RefType.CARE, careId);

        return CareResponseDto.from(care, images);
    }

    /**
     * 돌봄/산책 요청 수정
     */
    @Transactional
    public CareResponseDto updateCare(Long careId, Long userId, CareRequestDto requestDto) {
        log.info("돌봄/산책 요청 수정 시작 - ID: {}, 사용자: {}", careId, userId);

        // 요청 데이터 유효성 검증
        requestDto.validate();

        Cares care = getCareById(careId);
        validateOwnership(care, userId);

        // 엔티티 조회
        Pet pet = getPetById(requestDto.getPetId());
        Region region = getRegionById(requestDto.getRegionId());

        // 반려동물 소유자 확인
        validatePetOwnership(pet, userId);

        // 날짜/시간 변환
        LocalDateTime[] datetimes = convertToDatetimes(requestDto);
        LocalDateTime startDatetime = datetimes[0];
        LocalDateTime endDatetime = datetimes[1];

        // 과거 날짜 확인
        validateFutureDateTime(startDatetime);

        // 정보 업데이트
        care.updateInfo(requestDto.getTitle(), requestDto.getContent(), pet, region,
                requestDto.getCategory(), startDatetime, endDatetime);

        // 기존 이미지 삭제 후 새 이미지 저장
        updateImages(careId, RefType.CARE, requestDto.getImages());

        log.info("돌봄/산책 요청 수정 완료 - ID: {}", careId);

        // 이미지 정보 포함해서 응답
        List<ImageResponse> images = imageService.getImages(RefType.CARE, careId);
        return CareResponseDto.from(care, images);
    }

    /**
     * 돌봄/산책 요청 삭제
     */
    @Transactional
    public void deleteCare(Long careId, Long userId) {
        log.info("돌봄/산책 요청 삭제 시작 - ID: {}, 사용자: {}", careId, userId);

        Cares care = getCareById(careId);
        validateOwnership(care, userId);

        care.delete();

        log.info("돌봄/산책 요청 삭제 완료 - ID: {}", careId);
    }

    /**
     * 돌봄/산책 요청 상태 변경
     */
    @Transactional
    public CareResponseDto updateCareStatus(Long careId, Long userId, CareStatus status) {
        log.info("돌봄/산책 요청 상태 변경 - ID: {}, 상태: {}", careId, status);

        Cares care = getCareById(careId);
        validateOwnership(care, userId);

        care.updateStatus(status);

        // 이미지 정보 포함해서 응답
        List<ImageResponse> images = imageService.getImages(RefType.CARE, careId);
        return CareResponseDto.from(care, images);
    }

    /**
     * 지역별 돌봄/산책 요청 목록 조회
     */
    public Page<CareListResponseDto> getCaresByRegion(Long regionId, Pageable pageable) {
        return careRepository.findByRegionIdAndNotDeleted(regionId, pageable)
                .map(care -> {
                    List<ImageResponse> images = imageService.getImages(RefType.CARE, care.getId());
                    return CareListResponseDto.from(care, images);
                });
    }

    /**
     * 카테고리별 돌봄/산책 요청 목록 조회
     */
    public Page<CareListResponseDto> getCaresByCategory(Long regionId, CareCategory category, Pageable pageable) {
        return careRepository.findByRegionIdAndCategoryAndNotDeleted(regionId, category, pageable)
                .map(care -> {
                    List<ImageResponse> images = imageService.getImages(RefType.CARE, care.getId());
                    return CareListResponseDto.from(care, images);
                });
    }

    /**
     * 상태별 돌봄/산책 요청 목록 조회
     */
    public Page<CareListResponseDto> getCaresByStatus(Long regionId, CareStatus status, Pageable pageable) {
        return careRepository.findByRegionIdAndStatusAndNotDeleted(regionId, status, pageable)
                .map(care -> {
                    List<ImageResponse> images = imageService.getImages(RefType.CARE, care.getId());
                    return CareListResponseDto.from(care, images);
                });
    }

    /**
     * 사용자별 돌봄/산책 요청 목록 조회
     */
    public Page<CareListResponseDto> getCaresByUser(Long userId, Pageable pageable) {
        return careRepository.findByUserIdAndNotDeleted(userId, pageable)
                .map(care -> {
                    List<ImageResponse> images = imageService.getImages(RefType.CARE, care.getId());
                    return CareListResponseDto.from(care, images);
                });
    }

    /**
     * 키워드 검색
     */
    public Page<CareListResponseDto> searchCares(Long regionId, String keyword, Pageable pageable) {
        return careRepository.findByRegionIdAndKeywordAndNotDeleted(regionId, keyword, pageable)
                .map(care -> {
                    List<ImageResponse> images = imageService.getImages(RefType.CARE, care.getId());
                    return CareListResponseDto.from(care, images);
                });
    }

    /**
     * 복합 조건 검색
     */
    public Page<CareListResponseDto> searchCaresWithConditions(Long regionId, CareCategory category,
                                                               CareStatus status, String keyword, Pageable pageable) {
        return careRepository.findByComplexConditionsAndNotDeleted(regionId, category, status, keyword, pageable)
                .map(care -> {
                    List<ImageResponse> images = imageService.getImages(RefType.CARE, care.getId());
                    return CareListResponseDto.from(care, images);
                });
    }

    // ===== 헬퍼 메서드들 =====

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Pet getPetById(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다."));
    }

    private Region getRegionById(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다."));
    }

    private Cares getCareById(Long careId) {
        return careRepository.findByIdAndNotDeleted(careId)
                .orElseThrow(() -> new IllegalArgumentException("돌봄/산책 요청을 찾을 수 없습니다."));
    }

    private void validateOwnership(Cares care, Long userId) {
        if (!care.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 등록한 요청만 수정/삭제할 수 있습니다.");
        }
    }

    private void validatePetOwnership(Pet pet, Long userId) {
        if (!pet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 반려동물만 등록할 수 있습니다.");
        }
    }

    private void validateFutureDateTime(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("과거 날짜/시간은 설정할 수 없습니다.");
        }
    }

    private LocalDateTime[] convertToDatetimes(CareRequestDto requestDto) {
        LocalDateTime startDatetime, endDatetime;

        if (isWalkCategory(requestDto.getCategory())) {
            // 산책: 같은 날짜의 시간 범위
            startDatetime = LocalDateTime.of(requestDto.getStartDate(), requestDto.getStartTime());
            endDatetime = LocalDateTime.of(requestDto.getStartDate(), requestDto.getEndTime());
        } else {
            // 돌봄: 날짜 범위 (시작일 00:00:00 ~ 종료일 23:59:59)
            startDatetime = requestDto.getStartDate().atStartOfDay();
            endDatetime = requestDto.getEndDate().atTime(23, 59, 59);
        }

        return new LocalDateTime[]{startDatetime, endDatetime};
    }

    private boolean isWalkCategory(CareCategory category) {
        return category == CareCategory.WALK_WANT || category == CareCategory.WALK_REQ;
    }

    /**
     * 이미지 저장 공통 메서드 (Feed 패키지와 동일한 방식)
     */
    private void saveImages(List<CareImageRequest> imageRequests, Long refId, RefType refType) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }

        for (CareImageRequest imgReq : imageRequests) {
            ImageRequest imageRequest = ImageRequest.builder()
                    .refId(refId)
                    .refType(refType)
                    .src(imgReq.getSrc())
                    .sort(imgReq.getSort())
                    .build();

            imageService.createImages(imageRequest);
            log.debug("이미지 저장 완료 - refId: {}, refType: {}, src: {}", refId, refType, imgReq.getSrc());
        }
    }

    /**
     * 이미지 업데이트 (Feed 패키지와 동일한 방식)
     */
    private void updateImages(Long refId, RefType refType, List<CareImageRequest> newImages) {
        // 기존 이미지 모두 삭제
        imageRepository.deleteAllByRef(refType, refId);

        // 새 이미지 저장
        if (newImages != null && !newImages.isEmpty()) {
            List<Image> toAdd = newImages.stream()
                    .map(ir -> new Image(refId, refType, ir.getSrc(), ir.getSort()))
                    .toList();
            imageRepository.saveAll(toAdd);
        }
    }
}