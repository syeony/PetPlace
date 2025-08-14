package com.minjeok4go.petplace.missing.service;

import com.minjeok4go.petplace.common.constant.Animal;
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
import com.minjeok4go.petplace.common.constant.RefType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MissingService {

    private final TaskExecutor appTaskExecutor; // ← 네가 만든 @Bean taskExecutor 주입

    private final MissingReportRepository missingReportRepository;
    private final SightingRepository sightingRepository;
    private final SightingMatchRepository sightingMatchRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final RegionRepository regionRepository;
    private final ImageService imageService; // 기존 ImageService 활용
    private final ImageRepository imageRepository; // 이미지 삭제용
    private final IndexingService indexingService; // ← 비동기 인덱싱 호출용

//    private final AIMatchingService aiMatchingService;

    /**
     * 실종 신고 등록
     */
    @Transactional
    public MissingReportResponse createMissingReport(Long userId, MissingReportCreateRequest request) {
        log.info("실종 신고 등록 시작 - 사용자: {}, 반려동물: {}", userId, request.getPetId());

        /* 1) 엔티티 조회/검증 */
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + request.getPetId()));

        // 소유권 체크 (내 반려동물인지)
        if (!pet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물의 소유자가 아닙니다");
        }

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다: " + request.getRegionId()));

        /* 2) 실종 신고 저장 (트랜잭션 내) */
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

        /* 3) 이미지 저장 (DB src는 /images/... , 실제 파일은 /data/images/에 저장되어 있어야 함) */
        saveImages(request.getImages(), missingReport.getId(), RefType.MISSING_REPORT);

        /* 4) 커밋 후 인덱싱 트리거 (비동기) */
        //   - 커밋까지 성공해야만 인덱싱 실행 → 인덱스 오염 방지
        final Long reportId = missingReport.getId();
        final String species = deduceSpeciesFromPet(pet); // "dog" | "cat" 로 매핑 (아래 헬퍼 참고)

        runAfterCommit(() -> {
            // ⚠️ 여기서 예외가 나더라도 본 트랜잭션엔 영향 없음(이미 커밋 완료)
            //    인덱싱 실패는 로깅/재시도(옵션)로 대응
            try {
                indexingService.indexMissingReportImages(reportId, species);
            } catch (Exception e) {
                log.error("커밋 후 인덱싱 실패 - reportId={}, species={}", reportId, species, e);
            }
        });

        /* 5) 응답 빌드 (방금 저장한 이미지 목록을 함께 내려주기) */
        List<ImageResponse> imageResponses = imageService.getImages(RefType.MISSING_REPORT, reportId);
        return MissingReportResponse.from(missingReport, imageResponses);
    }

    /**
     * 트랜잭션 "커밋 후(afterCommit)"에 작업을 실행하는 헬퍼.
     * - 실제 트랜잭션이 활성화되어 있으면 afterCommit 훅에 등록
     * - 트랜잭션이 없으면(비정상 경로/테스트) 즉시 실행
     * - Spring 6에서는 TransactionSynchronizationAdapter가 제거됨 → TransactionSynchronization 사용
     */
    private void runAfterCommit(Runnable task) {
        Runnable safe = () -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("afterCommit 작업 실행 중 오류", e);
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    appTaskExecutor.execute(safe); // ✅ 비동기 실행만
                }
            });
        } else {
            appTaskExecutor.execute(safe);         // ✅ 트랜잭션 없을 때도 비동기만
        }
    }

    /**
     * Pet 엔티티에서 검색 엔진이 요구하는 species 문자열("dog"/"cat")을 도출.
     * - 프로젝트 도메인에 따라 enum/문자열 필드 중 하나를 사용
     * - 값이 모호하면 "dog"를 안전 기본값으로 사용
     */
    // MissingService 안에 헬퍼로 추가
    private String deduceSpeciesFromPet(Pet pet) {
        if (pet == null || pet.getAnimal() == null) return "dog"; // 기본값
        return (pet.getAnimal() == Animal.CAT) ? "cat" : "dog";
    }

    // Sighting용: 클라이언트가 보낸 species 문자열을 "dog"/"cat"으로 안전 정규화
    private String safeSpecies(String species) {
        if (species == null) return "dog"; // 기본값(없으면 dog)
        String s = species.trim().toLowerCase(java.util.Locale.ROOT);

        // 한글/영문 모두 허용
        if (s.contains("cat") || s.contains("고양")) return "cat";
        if (s.contains("dog") || s.contains("강아지") || s.contains("개")) return "dog";

        // 알 수 없는 값이면 기본값
        return "dog";
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
        saveImages(request.getImages(), sighting.getId(), RefType.SIGHTING);

        // 4. 이미지 조회하여 Response 생성
        List<ImageResponse> imageResponses = imageService.getImages(RefType.SIGHTING, sighting.getId());

        // 5) 커밋 후 비동기 매칭 트리거
        //    - 커밋 성공 후에만 FastAPI를 호출(롤백 시 인덱스/매칭 오염 방지)
        final Long sightingId = sighting.getId();
        final String species = safeSpecies(request.getSpecies());   // "dog" | "cat" (요청에 없으면 기본 dog)
        final Integer xmin = request.getXmin();
        final Integer ymin = request.getYmin();
        final Integer xmax = request.getXmax();
        final Integer ymax = request.getYmax();
        final Double  wFace = request.getWFace();                   // null 이면 서버 기본값 사용
        final String  firstImgSrc = imageResponses.isEmpty() ? null : imageResponses.get(0).getSrc(); // 대표 1장



        runAfterCommit(() -> {
            if (firstImgSrc == null) {
                log.warn("매칭 생략: 이미지 없음 sightingId={}", sightingId);
                return;
            }
            try {
                // IndexingService가 /images/** → /data/images/**로 매핑 후
                // aiSimilarityClient.searchStreaming(...) 호출해서 결과를 SightingMatch에 저장
                indexingService.searchForSighting(
                        sightingId,
                        species,
                        firstImgSrc,        // "/images/sighting_images/xxx.jpg"
                        xmin, ymin, xmax, ymax,
                        30,                 // topK
                        wFace
                );
            } catch (Exception e) {
                log.error("목격 매칭 트리거 실패 sightingId={}", sightingId, e);
            }
        });

        // 6) 즉시 응답 (비동기 매칭은 백그라운드에서 진행)
        return SightingResponse.from(sighting, imageResponses);
    }//    /**
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

        List<ImageResponse> images = imageService.getImages(RefType.MISSING_REPORT, id);
        return MissingReportResponse.from(missingReport, images);
    }

    /**
     * 목격 제보 상세 조회
     */
    public SightingResponse getSighting(Long id) {
        Sighting sighting = sightingRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("목격 제보를 찾을 수 없습니다: " + id));

        List<ImageResponse> images = imageService.getImages(RefType.SIGHTING, id);
        return SightingResponse.from(sighting, images);
    }

    /**
     * 지역별 실종 신고 목록 조회
     */
    public Page<MissingReportResponse> getMissingReportsByRegion(Long regionId, Pageable pageable) {
        Page<MissingReport> missingReports = missingReportRepository.findByRegionIdAndNotDeleted(regionId, pageable);

        return missingReports.map(report -> {
            List<ImageResponse> images = imageService.getImages(
                    RefType.MISSING_REPORT, report.getId());
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
                    RefType.SIGHTING, sighting.getId());
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
                    RefType.MISSING_REPORT, report.getId());
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
                     RefType.SIGHTING, match.getSighting().getId());
            SightingResponse sightingResponse = SightingResponse.from(match.getSighting(), sightingImages);

            // 실종 신고 이미지 조회
            List<ImageResponse> missingImages = imageService.getImages(
                    RefType.MISSING_REPORT, match.getMissingReport().getId());
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

        List<ImageResponse> images = imageService.getImages(RefType.MISSING_REPORT, id);
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
                RefType.SIGHTING, match.getSighting().getId() );
        SightingResponse sightingResponse = SightingResponse.from(match.getSighting(), sightingImages);

        List<ImageResponse> missingImages = imageService.getImages(
                RefType.MISSING_REPORT,match.getMissingReport().getId());
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
                     RefType.SIGHTING, match.getSighting().getId());
            SightingResponse sightingResponse = SightingResponse.from(match.getSighting(), sightingImages);

            List<ImageResponse> missingImages = imageService.getImages(
                     RefType.MISSING_REPORT, match.getMissingReport().getId());
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
    private void saveImages(List<MissingImageRequest> imageRequests, Long refId, RefType refType) {
        if (imageRequests == null || imageRequests.isEmpty()) return;

        // 중복 src 방지용 (선택)
        Set<String> seen = new HashSet<>();
        int idx = 0;

        for (MissingImageRequest imgReq : imageRequests) {
            if (imgReq == null) continue;

            // 1) src 기본 검증 (정적 경로 규칙 보장)
            String src = imgReq.getSrc();
            if (src == null || !src.startsWith("/images/")) {
                throw new IllegalArgumentException("잘못된 이미지 경로: " + src);
            }
            if (!seen.add(src)) {
                // 같은 요청 내 중복 이미지가 있으면 스킵(선택)
                continue;
            }

            // 2) sort 기본값 처리 (null이면 순서대로)
            Integer sort = imgReq.getSort();
            if (sort == null) sort = idx;

            ImageRequest imageRequest = ImageRequest.builder()
                    .refId(refId)
                    .refType(refType)
                    .src(src)
                    .sort(sort)
                    .build();

            // 3) 실제 저장
            try {
                imageService.createImages(imageRequest); // 단건 저장 메서드라면 이대로
                // imageService.createImages(List<ImageRequest>) 형태의 배치가 있다면
                // 위 루프를 List로 모은 뒤 한 번에 호출하는 게 DB round-trip에 유리
            } catch (Exception e) {
                // 한 장 실패해도 나머지 진행 (필요시 정책에 맞게 조정)
                log.warn("이미지 저장 실패 - refId={}, refType={}, src={}", refId, refType, src, e);
            }

            idx++;
            log.debug("이미지 저장 완료 - refId: {}, refType: {}, src: {}, sort: {}", refId, refType, src, sort);
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
        updateImages(missingReport.getId(), RefType.MISSING_REPORT, request.getImages());

        // 수정된 정보 조회하여 반환
        List<ImageResponse> imageResponses = imageService.getImages(RefType.MISSING_REPORT, missingReport.getId());
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
        updateImages(sighting.getId(), RefType.SIGHTING, request.getImages());

        // 수정된 정보 조회하여 반환
        List<ImageResponse> imageResponses = imageService.getImages(RefType.SIGHTING, sighting.getId());
        return SightingResponse.from(sighting, imageResponses);
    }

    /**
     * 이미지 업데이트 (기존 이미지 삭제 후 새 이미지 저장)
     */
    private void updateImages(Long refId, RefType refType, List<MissingImageRequest> newImages) {
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