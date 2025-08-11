package com.minjeok4go.petplace.missing.service;

import com.minjeok4go.petplace.missing.dto.MissingReportCreateRequest;
import com.minjeok4go.petplace.missing.dto.SightingCreateRequest;
import com.minjeok4go.petplace.missing.dto.MissingImageRequest;
import com.minjeok4go.petplace.missing.dto.MissingReportResponse;
import com.minjeok4go.petplace.missing.dto.SightingResponse;
import com.minjeok4go.petplace.missing.dto.SightingMatchResponse;
import com.minjeok4go.petplace.missing.entity.MissingReport;
import com.minjeok4go.petplace.missing.entity.Sighting;
import com.minjeok4go.petplace.missing.entity.SightingMatch;
import com.minjeok4go.petplace.missing.repository.MissingReportRepository;
import com.minjeok4go.petplace.missing.repository.SightingRepository;
import com.minjeok4go.petplace.missing.repository.SightingMatchRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.pet.repository.PetRepository;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.region.repository.RegionRepository;
import com.minjeok4go.petplace.image.service.ImageService;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.common.constant.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MissingService {

    private final MissingReportRepository missingReportRepository;
    private final SightingRepository sightingRepository;
    private final SightingMatchRepository sightingMatchRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final RegionRepository regionRepository;
    private final ImageService imageService; // 기존 ImageService 활용
    private final ImageRepository imageRepository; // 이미지 삭제용
//    private final AIMatchingService aiMatchingService;

    /**
     * 실종 신고 등록
     */
    @Transactional
    public MissingReportResponse createMissingReport(Long userId, MissingReportCreateRequest request) {
        log.info("실종 신고 등록 시작 - 사용자: {}, 반려동물: {}", userId, request.getPetId());

        // 1. 엔티티 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + request.getPetId()));

        // 반려동물 소유권 확인
        if (!pet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물의 소유자가 아닙니다");
        }

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다: " + request.getRegionId()));

        // 2. 실종 신고 엔티티 생성 및 저장
        MissingReport missingReport = MissingReport.builder()
                .user(user)
                .pet(pet)
                .region(region)
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .content(request.getContent())
                .missingAt(request.getMissingAt())
                .build();

        missingReport = missingReportRepository.save(missingReport);
        log.info("실종 신고 저장 완료 - ID: {}", missingReport.getId());

        // 3. 이미지 저장 (기존 ImageService 활용)
        saveImages(request.getImages(), missingReport.getId(), ImageType.MISSING_REPORT);

        // 4. 이미지 조회하여 Response 생성
        List<ImageResponse> imageResponses = imageService.getImages(ImageType.MISSING_REPORT, missingReport.getId());

        return MissingReportResponse.from(missingReport, imageResponses);
    }

    /**
     * 목격 제보 등록
     */
    @Transactional
    public SightingResponse createSighting(Long userId, SightingCreateRequest request) {
        log.info("목격 제보 등록 시작 - 사용자: {}", userId);

        // 1. 엔티티 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다: " + request.getRegionId()));

        // 2. 목격 제보 엔티티 생성 및 저장
        Sighting sighting = Sighting.builder()
                .user(user)
                .region(region)
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .content(request.getContent())
                .sightedAt(request.getSightedAt())
                .build();

        sighting = sightingRepository.save(sighting);
        log.info("목격 제보 저장 완료 - ID: {}", sighting.getId());

        // 3. 이미지 저장 (기존 ImageService 활용)
        saveImages(request.getImages(), sighting.getId(), ImageType.SIGHTING);

        // 4. 이미지 조회하여 Response 생성
        List<ImageResponse> imageResponses = imageService.getImages(ImageType.SIGHTING, sighting.getId());

//        // 5. 비동기로 AI 품종 예측 및 자동 매칭 수행
//        performAIProcessing(sighting, request.getImages());

        return SightingResponse.from(sighting, imageResponses);
    }

//    /**
//     * AI 처리 (품종 예측 + 자동 매칭)를 비동기로 실행 하고싶은데 일단 모르겠으니까 주석 처리
//     */
//    @Async
//    @Transactional
//    public void performAIProcessing(Sighting sighting, List<MissingImageRequest> images) {
//        try {
//            log.info("목격 제보 ID: {}에 대한 AI 처리 시작", sighting.getId());
//
//            // 1. 첫 번째 이미지로 품종 예측 (Mock - 실제로는 이미지 URL로 AI 호출)
//            if (images != null && !images.isEmpty()) {
//                String firstImageUrl = images.get(0).getSrc();
//                Pet.Breed predictedBreed = aiMatchingService.predictBreedFromUrl(firstImageUrl);
//
//                // 예측 결과를 DB에 저장
//                Sighting savedSighting = sightingRepository.findById(sighting.getId())
//                        .orElseThrow(() -> new IllegalArgumentException("목격 제보를 찾을 수 없습니다"));
//
//                savedSighting.updatePredictedBreed(predictedBreed);
//                sightingRepository.save(savedSighting);
//
//                log.info("품종 예측 완료 - 목격제보: {}, 예측품종: {}",
//                        sighting.getId(), predictedBreed.name());
//
//                // 2. 자동 매칭 수행
//                aiMatchingService.performAutoMatching(savedSighting);
//            }
//
//        } catch (Exception e) {
//            log.error("AI 처리 중 오류 발생 - 목격제보 ID: {}", sighting.getId(), e);
//        }
//    }

    /**
     * 실종 신고 상세 조회
     */
    public MissingReportResponse getMissingReport(Long id) {
        MissingReport missingReport = missingReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("실종 신고를 찾을 수 없습니다: " + id));

        List<ImageResponse> images = imageService.getImages(ImageType.MISSING_REPORT, id);
        return MissingReportResponse.from(missingReport, images);
    }

    /**
     * 목격 제보 상세 조회
     */
    public SightingResponse getSighting(Long id) {
        Sighting sighting = sightingRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("목격 제보를 찾을 수 없습니다: " + id));

        List<ImageResponse> images = imageService.getImages(ImageType.SIGHTING, id);
        return SightingResponse.from(sighting, images);
    }

    /**
     * 지역별 실종 신고 목록 조회
     */
    public Page<MissingReportResponse> getMissingReportsByRegion(Long regionId, Pageable pageable) {
        Page<MissingReport> missingReports = missingReportRepository.findByRegionIdAndNotDeleted(regionId, pageable);

        return missingReports.map(report -> {
            List<ImageResponse> images = imageService.getImages(
                    ImageType.MISSING_REPORT, report.getId());
            return MissingReportResponse.from(report, images);
        });
    }

    /**
     * 지역별 목격 제보 목록 조회
     */
    public Page<SightingResponse> getSightingsByRegion(Long regionId, Pageable pageable) {
        Page<Sighting> sightings = sightingRepository.findByRegionIdAndNotDeleted(regionId, pageable);

        return sightings.map(sighting -> {
            List<ImageResponse> images = imageService.getImages(
                    ImageType.SIGHTING, sighting.getId());
            return SightingResponse.from(sighting, images);
        });
    }

    /**
     * 사용자별 실종 신고 목록 조회
     */
    public Page<MissingReportResponse> getMissingReportsByUser(Long userId, Pageable pageable) {
        Page<MissingReport> missingReports = missingReportRepository.findByUserIdAndNotDeleted(userId, pageable);

        return missingReports.map(report -> {
            List<ImageResponse> images = imageService.getImages(
                    ImageType.MISSING_REPORT, report.getId());
            return MissingReportResponse.from(report, images);
        });
    }

    /**
     * 특정 실종 신고의 매칭 결과 조회
     */
    public Page<SightingMatchResponse> getMatchesForMissingReport(Long missingReportId, Pageable pageable) {
        // 실종 신고 존재 확인
        missingReportRepository.findByIdAndNotDeleted(missingReportId)
                .orElseThrow(() -> new IllegalArgumentException("실종 신고를 찾을 수 없습니다: " + missingReportId));

        Page<SightingMatch> matches = sightingMatchRepository
                .findByMissingReportIdOrderByScoreDesc(missingReportId, pageable);

        return matches.map(match -> {
            // 목격 제보 이미지 조회
            List<ImageResponse> sightingImages = imageService.getImages(
                     ImageType.SIGHTING, match.getSighting().getId());
            SightingResponse sightingResponse = SightingResponse.from(match.getSighting(), sightingImages);

            // 실종 신고 이미지 조회
            List<ImageResponse> missingImages = imageService.getImages(
                    ImageType.MISSING_REPORT, match.getMissingReport().getId());
            MissingReportResponse missingResponse = MissingReportResponse.from(match.getMissingReport(), missingImages);

            return SightingMatchResponse.from(match, sightingResponse, missingResponse);
        });
    }

    /**
     * 실종 신고 상태 변경 (찾음/취소)
     */
    @Transactional
    public MissingReportResponse updateMissingReportStatus(Long id, Long userId, MissingReport.MissingStatus status) {
        MissingReport missingReport = missingReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("실종 신고를 찾을 수 없습니다: " + id));

        // 소유권 확인
        if (!missingReport.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("실종 신고를 수정할 권한이 없습니다");
        }

        missingReport.updateStatus(status);
        missingReport = missingReportRepository.save(missingReport);

        List<ImageResponse> images = imageService.getImages(ImageType.MISSING_REPORT, id);
        return MissingReportResponse.from(missingReport, images);
    }

    /**
     * 매칭 상태 변경 (확인/거부)
     */
    @Transactional
    public SightingMatchResponse updateMatchStatus(Long matchId, Long userId, SightingMatch.MatchStatus status) {
        SightingMatch match = sightingMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다: " + matchId));

        // 실종 신고 소유자만 매칭 상태를 변경할 수 있음
        if (!match.getMissingReport().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("매칭 상태를 변경할 권한이 없습니다");
        }

        match.updateStatus(status);
        match = sightingMatchRepository.save(match);

        // Response DTO 생성
        List<ImageResponse> sightingImages = imageService.getImages(
                ImageType.SIGHTING, match.getSighting().getId() );
        SightingResponse sightingResponse = SightingResponse.from(match.getSighting(), sightingImages);

        List<ImageResponse> missingImages = imageService.getImages(
                ImageType.MISSING_REPORT,match.getMissingReport().getId());
        MissingReportResponse missingResponse = MissingReportResponse.from(match.getMissingReport(), missingImages);

        return SightingMatchResponse.from(match, sightingResponse, missingResponse);
    }

    /**
     * 사용자의 대기 중인 매칭 알림 조회
     */
    public Page<SightingMatchResponse> getPendingMatchesForUser(Long userId, Pageable pageable) {
        // 높은 점수 매칭만 알림으로 표시 (예: 0.7 이상)
        BigDecimal minScore = new BigDecimal("0.7");

        Page<SightingMatch> matches = sightingMatchRepository
                .findPendingMatchesForUser(userId, minScore, pageable);

        return matches.map(match -> {
            List<ImageResponse> sightingImages = imageService.getImages(
                     ImageType.SIGHTING, match.getSighting().getId());
            SightingResponse sightingResponse = SightingResponse.from(match.getSighting(), sightingImages);

            List<ImageResponse> missingImages = imageService.getImages(
                     ImageType.MISSING_REPORT, match.getMissingReport().getId());
            MissingReportResponse missingResponse = MissingReportResponse.from(match.getMissingReport(), missingImages);

            return SightingMatchResponse.from(match, sightingResponse, missingResponse);
        });
    }

    /**
     * 실종 신고 삭제 (소프트 딜리트)
     */
    @Transactional
    public void deleteMissingReport(Long id, Long userId) {
        MissingReport missingReport = missingReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("실종 신고를 찾을 수 없습니다: " + id));

        // 소유권 확인
        if (!missingReport.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("실종 신고를 삭제할 권한이 없습니다");
        }

        missingReport.delete();
        missingReportRepository.save(missingReport);
        log.info("실종 신고 삭제 완료 - ID: {}", id);
    }

    /**
     * 목격 제보 삭제 (소프트 딜리트)
     */
    @Transactional
    public void deleteSighting(Long id, Long userId) {
        Sighting sighting = sightingRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("목격 제보를 찾을 수 없습니다: " + id));

        // 소유권 확인
        if (!sighting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("목격 제보를 삭제할 권한이 없습니다");
        }

        sighting.delete();
        sightingRepository.save(sighting);
        log.info("목격 제보 삭제 완료 - ID: {}", id);
    }

    /**
     * 이미지 저장 공통 메서드 (기존 ImageService 활용)
     */
    private void saveImages(List<MissingImageRequest> imageRequests, Long refId, ImageType refType) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }

        for (MissingImageRequest imgReq : imageRequests) {
            ImageRequest imageRequest = ImageRequest.builder()
                    .refId(refId)
                    .refType(refType)
                    .src(imgReq.getSrc())
                    .sort(imgReq.getSort())
                    .build();

            imageService.upload(imageRequest);
            log.debug("이미지 저장 완료 - refId: {}, refType: {}, src: {}", refId, refType, imgReq.getSrc());
        }
    }

    /**
     * 실종 신고 수정
     */
    @Transactional
    public MissingReportResponse updateMissingReport(Long id, Long userId, MissingReportCreateRequest request) {
        log.info("실종 신고 수정 시작 - ID: {}, 사용자: {}", id, userId);

        // 기존 실종 신고 조회 및 권한 확인
        MissingReport missingReport = missingReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("실종 신고를 찾을 수 없습니다: " + id));

        if (!missingReport.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("실종 신고를 수정할 권한이 없습니다");
        }

        // 엔티티 검증
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + request.getPetId()));

        if (!pet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물의 소유자가 아닙니다");
        }

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다: " + request.getRegionId()));

        // 실종 신고 정보 업데이트
        missingReport.updateInfo(
                pet,
                region,
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getContent(),
                request.getMissingAt()
        );

        missingReport = missingReportRepository.save(missingReport);

        // 기존 이미지 삭제 후 새 이미지 저장
        updateImages(missingReport.getId(), ImageType.MISSING_REPORT, request.getImages());

        // 수정된 정보 조회하여 반환
        List<ImageResponse> imageResponses = imageService.getImages(ImageType.MISSING_REPORT, missingReport.getId());
        return MissingReportResponse.from(missingReport, imageResponses);
    }

    /**
     * 목격 제보 수정
     */
    @Transactional
    public SightingResponse updateSighting(Long id, Long userId, SightingCreateRequest request) {
        log.info("목격 제보 수정 시작 - ID: {}, 사용자: {}", id, userId);

        // 기존 목격 제보 조회 및 권한 확인
        Sighting sighting = sightingRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("목격 제보를 찾을 수 없습니다: " + id));

        if (!sighting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("목격 제보를 수정할 권한이 없습니다");
        }

        // 엔티티 검증
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다: " + request.getRegionId()));

        // 목격 제보 정보 업데이트
        sighting.updateInfo(
                region,
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getContent(),
                request.getSightedAt()
        );

        sighting = sightingRepository.save(sighting);

        // 기존 이미지 삭제 후 새 이미지 저장
        updateImages(sighting.getId(), ImageType.SIGHTING, request.getImages());

        // 수정된 정보 조회하여 반환
        List<ImageResponse> imageResponses = imageService.getImages(ImageType.SIGHTING, sighting.getId());
        return SightingResponse.from(sighting, imageResponses);
    }

    /**
     * 이미지 업데이트 (기존 이미지 삭제 후 새 이미지 저장)
     */
    private void updateImages(Long refId, ImageType refType, List<MissingImageRequest> newImages) {
        // 기존 이미지 조회
        List<Image> existingImages = imageRepository.findByRefTypeAndRefIdOrderBySortAsc(refType, refId);
        
        // 기존 이미지 삭제
        if (!existingImages.isEmpty()) {
            imageRepository.deleteAll(existingImages);
        }

        // 새 이미지 저장
        saveImages(newImages, refId, refType);
    }
}