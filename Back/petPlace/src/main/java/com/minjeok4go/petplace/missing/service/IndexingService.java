package com.minjeok4go.petplace.missing.service;

import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.image.service.ImageService;
import com.minjeok4go.petplace.missing.client.AiSimilarityClient;
import com.minjeok4go.petplace.missing.entity.MissingReport;
import com.minjeok4go.petplace.missing.entity.Sighting;
import com.minjeok4go.petplace.missing.entity.SightingMatch;
import com.minjeok4go.petplace.missing.repository.MissingReportRepository;
import com.minjeok4go.petplace.missing.repository.SightingMatchRepository;
import com.minjeok4go.petplace.missing.repository.SightingRepository;
import com.minjeok4go.petplace.notification.dto.CreateCommentNotificationRequest;
import com.minjeok4go.petplace.notification.dto.CreateSightingNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingService {

    private final ImageService imageService;
    private final AiSimilarityClient aiClient;
    private static final int MAX_SAVE = 5;
    private static final double MIN_SCORE = 0.35;

    private final ApplicationEventPublisher publisher;
    private final ImageRepository imageRepository;
    private final MissingReportRepository missingReportRepository;
    private final SightingMatchRepository sightingMatchRepository;
    private final SightingRepository sightingRepository; // ✅ 추가

    // 컨테이너 내부 통일 경로(/data/images) – compose로 마운트됨
    @Value("${app.upload.base:/data/images}")
    private String uploadBase;

    /** DB src("/images/...") → 컨테이너 절대경로("/data/images/...") */
    private Path toContainerPath(String webSrc) {
        if (webSrc == null || !webSrc.startsWith("/images/")) return null;
        String rel = webSrc.substring("/images".length());   // "/lost_pets_images/xxx.jpg"
        return Path.of(uploadBase + rel);                    // "/data/images/lost_pets_images/xxx.jpg"
    }

    /** 실종 신고 저장 후: 등록 이미지들을 인덱스에 추가 */
    @Async
    public void indexMissingReportImages(Long missingReportId, String species) {


        // 1) MissingReport -> Pet -> breed 가져오기
        String breedEng = missingReportRepository.findPetBreedByReportId(missingReportId)
                .map(Enum::name).map(IndexingService::normBreed).orElse(null);

        List<ImageResponse> images = imageService.getImages(RefType.MISSING_REPORT, missingReportId);
        if (images == null || images.isEmpty()) {
            log.warn("indexMissingReportImages: no images (reportId={})", missingReportId);
            return;
        }
        for (ImageResponse ir : images) {
            try {
                Path p = toContainerPath(ir.getSrc());
                if (p == null) {
                    log.warn("invalid src: {}", ir.getSrc());
                    continue;
                }
                aiClient.indexAddPath(ir.getId(), species, p.toString(), breedEng,null, null, null, null, null).block();
            } catch (Exception e) {
                log.error("indexing failed: imageId={}, src={}", ir.getId(), ir.getSrc(), e);
            }
        }
        log.info("indexMissingReportImages done: reportId={}, count={}", missingReportId, images.size());
    }
    // class IndexingService { ... 맨 아래 아무데나
    private static String normBreed(String s) {
        if (s == null) return null;
        return s.trim().toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");
    }

    @Async
    @Transactional
    public void searchForSighting(Long sightingId, String species, String src,
                                  Integer xmin, Integer ymin, Integer xmax, Integer ymax,
                                  Integer topK, Double wFace) {
        try {
            String abs = Optional.ofNullable(toContainerPath(src))
                    .map(Path::toString).orElse(null);
            if (abs == null) {
                log.warn("searchForSighting: invalid src (null/unsupported) sightingId={}, src={}", sightingId, src);
                return;
            }
            var sighting = sightingRepository.getReferenceById(sightingId);

            String qBreedEng = Optional.ofNullable(sighting.getBreed())
                    .map(Enum::name)                 // "MALTESE_DOG"
                    .map(IndexingService::normBreed) // "maltesedog"
                    .orElse(null);

            var resp = aiClient.searchPath(
                    species,      // 소문자 처리는 AiSimilarityClient에서 함
                    abs,
                    qBreedEng,    // ★ 품종 추가 (null이면 미적용)
                    topK, wFace,
                    xmin, ymin, xmax, ymax
            ).block();

            if (resp == null || resp.getResults() == null || resp.getResults().isEmpty()) {
                log.info("searchForSighting: 후보 없음 sightingId={}, src={}", sightingId, src);
                return;
            }

            // JPA 프록시로 연관 세팅(쿼리 안 나감)
            var sightingRef = sighting; // 이미 getReferenceById 했으니 재사용

            int saved = 0;
            for (var it : resp.getResults()) {
                double score = it.getScore();
                if (score < MIN_SCORE) continue;

                var imgOpt = imageRepository.findById(it.getId());
                if (imgOpt.isEmpty()) continue;
                var img = imgOpt.get();

                if (img.getRefType() != RefType.MISSING_REPORT) continue;

                var mrOpt = missingReportRepository.findById(img.getRefId());
                if (mrOpt.isEmpty()) continue;
                var mr = mrOpt.get();

                if (sightingMatchRepository.existsBySightingIdAndMissingReportId(sightingId, mr.getId())) {
                    continue;
                }

                var match = SightingMatch.builder()
                        .sighting(sightingRef)
                        .missingReport(mr)
                        .image(img)
                        .score(BigDecimal.valueOf(score))
                        .build();
                sightingMatchRepository.save(match);

                publisher.publishEvent(new CreateSightingNotificationRequest(
                        mr.getUser().getId(), sighting.getUser().getNickname(), RefType.SIGHTING, sighting.getId(), mr, sighting
                ));

                log.debug("{}에게 {}글이 매칭 되었음", mr.getUser().getId(), sighting.getId());

                if (++saved >= MAX_SAVE) break;
            }

            log.info("searchForSighting done: sightingId={}, saved={}", sightingId, saved);

        } catch (Exception e) {
            log.error("searchForSighting error sightingId={}, src={}", sightingId, src, e);
        }
    }

}