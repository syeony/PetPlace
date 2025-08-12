package com.minjeok4go.petplace.missing.repository;

import com.minjeok4go.petplace.missing.entity.SightingMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SightingMatchRepository extends JpaRepository<SightingMatch, Long> {

    // 특정 목격 제보의 매칭 결과 조회
    @Query("SELECT sm FROM SightingMatch sm " +
            "WHERE sm.sighting.id = :sightingId " +
            "ORDER BY sm.score DESC")
    List<SightingMatch> findBySightingIdOrderByScoreDesc(@Param("sightingId") Long sightingId);

    // 특정 실종 신고의 매칭 결과 조회
    @Query("SELECT sm FROM SightingMatch sm " +
            "WHERE sm.missingReport.id = :missingReportId " +
            "ORDER BY sm.score DESC, sm.createdAt DESC")
    Page<SightingMatch> findByMissingReportIdOrderByScoreDesc(@Param("missingReportId") Long missingReportId, Pageable pageable);

    // 사용자별 매칭 알림 조회 (실종 신고자 기준)
    @Query("SELECT sm FROM SightingMatch sm " +
            "WHERE sm.missingReport.user.id = :userId " +
            "AND sm.status = 'PENDING' " +
            "AND sm.score >= :minScore " +
            "ORDER BY sm.createdAt DESC")
    Page<SightingMatch> findPendingMatchesForUser(@Param("userId") Long userId,
                                                  @Param("minScore") BigDecimal minScore,
                                                  Pageable pageable);

    // 중복 매칭 방지를 위한 조회
    @Query("SELECT COUNT(sm) > 0 FROM SightingMatch sm " +
            "WHERE sm.sighting.id = :sightingId " +
            "AND sm.missingReport.id = :missingReportId")
    boolean existsBySightingIdAndMissingReportId(@Param("sightingId") Long sightingId,
                                                 @Param("missingReportId") Long missingReportId);

    // 높은 점수 매칭 통계
    @Query("SELECT COUNT(sm) FROM SightingMatch sm " +
            "WHERE sm.score >= :minScore " +
            "AND sm.status = 'PENDING'")
    Long countHighScoreMatches(@Param("minScore") BigDecimal minScore);
}