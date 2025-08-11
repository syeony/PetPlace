//package com.minjeok4go.petplace.missing.service;
//
//import com.minjeok4go.petplace.missing.entity.MissingReport;
//import com.minjeok4go.petplace.missing.entity.Sighting;
//import com.minjeok4go.petplace.missing.entity.SightingMatch;
//import com.minjeok4go.petplace.missing.repository.MissingReportRepository;
//import com.minjeok4go.petplace.missing.repository.SightingMatchRepository;
//import com.minjeok4go.petplace.common.constant.Breed;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AIMatchingService {
//
//    private final MissingReportRepository missingReportRepository;
//    private final SightingMatchRepository sightingMatchRepository;
//
//    /**
//     * 품종 예측을 위한 AI 모델 호출 (Mock 구현)
//     * 실제로는 TensorFlow Lite 모델이나 외부 AI API를 호출
//     */
//    public Breed predictBreed(byte[] imageData) {
//        // TODO: 실제 AI 모델 구현
//        // 현재는 Mock 데이터로 대체
//        log.info("AI 모델로 품종 예측 중...");
//
//        // 실제 구현 시에는 다음과 같은 로직이 들어갈 것입니다:
//        // 1. 이미지 전처리
//        // 2. TensorFlow Lite 모델 로드
//        // 3. 모델 추론 실행
//        // 4. 결과 후처리
//
//        // Mock 결과 반환 (임시)
//        return Breed.GOLDEN_RETRIEVER;
//    }
//
//    /**
//     * 목격 제보와 실종 신고 간의 자동 매칭 수행
//     */
//    @Async
//    @Transactional
//    public void performAutoMatching(Sighting sighting) {
//        log.info("목격 제보 ID: {}에 대한 자동 매칭 시작", sighting.getId());
//
//        try {
//            // 1. 매칭 범위 설정 (목격 위치 기준 5km 반경)
//            BigDecimal latRange = new BigDecimal("0.045"); // 약 5km
//            BigDecimal lngRange = new BigDecimal("0.045");
//
//            BigDecimal minLat = sighting.getLatitude().subtract(latRange);
//            BigDecimal maxLat = sighting.getLatitude().add(latRange);
//            BigDecimal minLng = sighting.getLongitude().subtract(lngRange);
//            BigDecimal maxLng = sighting.getLongitude().add(lngRange);
//
//            // 2. 최근 30일 이내의 실종 신고만 대상으로 설정
//            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
//
//            // 3. 범위 내의 활성화된 실종 신고 조회
//            List<MissingReport> nearbyReports = missingReportRepository.findActiveReportsInRange(
//                    minLat, maxLat, minLng, maxLng, thirtyDaysAgo
//            );
//
//            log.info("매칭 대상 실종 신고 {}건 발견", nearbyReports.size());
//
//            // 4. 각 실종 신고와의 유사도 계산 및 매칭 생성
//            for (MissingReport report : nearbyReports) {
//                // 중복 매칭 방지
//                if (sightingMatchRepository.existsBySightingIdAndMissingReportId(
//                        sighting.getId(), report.getId())) {
//                    continue;
//                }
//
//                // 유사도 계산
//                BigDecimal score = calculateSimilarityScore(sighting, report);
//
//                // 임계값 이상인 경우에만 매칭 생성 (예: 0.6 이상)
//                if (score.compareTo(new BigDecimal("0.6")) >= 0) {
//                    SightingMatch match = SightingMatch.builder()
//                            .sighting(sighting)
//                            .missingReport(report)
//                            .score(score)
//                            .build();
//
//                    sightingMatchRepository.save(match);
//                    log.info("매칭 생성: 목격제보 {} <-> 실종신고 {}, 점수: {}",
//                            sighting.getId(), report.getId(), score);
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("자동 매칭 중 오류 발생: ", e);
//        }
//    }
//
//    /**
//     * 목격 제보와 실종 신고 간의 유사도 계산
//     */
//    private BigDecimal calculateSimilarityScore(Sighting sighting, MissingReport report) {
//        BigDecimal totalScore = BigDecimal.ZERO;
//        BigDecimal maxScore = BigDecimal.ZERO;
//
//        // 1. 품종 매칭 (가중치: 0.5) - 가장 중요한 요소
//        maxScore = maxScore.add(new BigDecimal("0.5"));
//        if (sighting.getBreed() != null &&
//                sighting.getBreed().equals(report.getPet().getBreed())) {
//            totalScore = totalScore.add(new BigDecimal("0.5"));
//        }
//
//        // 2. 거리 매칭 (가중치: 0.3)
//        maxScore = maxScore.add(new BigDecimal("0.3"));
//        BigDecimal distance = calculateDistance(
//                sighting.getLatitude(), sighting.getLongitude(),
//                report.getLatitude(), report.getLongitude()
//        );
//
//        // 거리별 점수 (1km 이내: 만점, 5km 이내: 부분 점수)
//        if (distance.compareTo(new BigDecimal("1.0")) <= 0) {
//            totalScore = totalScore.add(new BigDecimal("0.3"));
//        } else if (distance.compareTo(new BigDecimal("5.0")) <= 0) {
//            BigDecimal distanceScore = new BigDecimal("0.3")
//                    .subtract(distance.multiply(new BigDecimal("0.06")));
//            totalScore = totalScore.add(distanceScore.max(BigDecimal.ZERO));
//        }
//
//        // 3. 시간 매칭 (가중치: 0.2)
//        maxScore = maxScore.add(new BigDecimal("0.2"));
//        long hoursDiff = Math.abs(
//                java.time.Duration.between(sighting.getSightedAt(), report.getMissingAt()).toHours()
//        );
//
//        // 24시간 이내: 만점, 7일 이내: 부분 점수
//        if (hoursDiff <= 24) {
//            totalScore = totalScore.add(new BigDecimal("0.2"));
//        } else if (hoursDiff <= 168) { // 7일
//            BigDecimal timeScore = new BigDecimal("0.2")
//                    .subtract(new BigDecimal(hoursDiff).multiply(new BigDecimal("0.001")));
//            totalScore = totalScore.add(timeScore.max(BigDecimal.ZERO));
//        }
//
//        // 최종 점수를 0-1 범위로 정규화
//        return totalScore.divide(maxScore, 4, BigDecimal.ROUND_HALF_UP);
//    }
//
//    /**
//     * 두 지점 간의 거리 계산 (단위: km)
//     */
//    private BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lng1,
//                                         BigDecimal lat2, BigDecimal lng2) {
//        // 하버사인 공식을 사용한 거리 계산
//        double earthRadius = 6371; // 지구 반지름 (km)
//
//        double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
//        double dLng = Math.toRadians(lng2.subtract(lng1).doubleValue());
//
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(Math.toRadians(lat1.doubleValue())) *
//                        Math.cos(Math.toRadians(lat2.doubleValue())) *
//                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
//
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double distance = earthRadius * c;
//
//        return new BigDecimal(distance).setScale(2, BigDecimal.ROUND_HALF_UP);
//    }
//}