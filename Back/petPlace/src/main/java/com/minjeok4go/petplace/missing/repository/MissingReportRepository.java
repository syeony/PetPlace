package com.minjeok4go.petplace.missing.repository;

import com.minjeok4go.petplace.common.constant.Breed;
import com.minjeok4go.petplace.missing.entity.MissingReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MissingReportRepository extends JpaRepository<MissingReport, Long> {

    // 삭제되지 않은 실종 신고 조회
    @Query("SELECT mr FROM MissingReport mr WHERE mr.deletedAt IS NULL AND mr.id = :id")
    Optional<MissingReport> findByIdAndNotDeleted(@Param("id") Long id);

    // 지역별 실종 신고 목록 조회 (페이징)
    @Query("SELECT mr FROM MissingReport mr " +
            "WHERE mr.deletedAt IS NULL " +
            "AND mr.region.id = :regionId " +
            "AND mr.status = 'MISSING' " +
            "ORDER BY mr.createdAt DESC")
    Page<MissingReport> findByRegionIdAndNotDeleted(@Param("regionId") Long regionId, Pageable pageable);

    // 사용자별 실종 신고 목록 조회
    @Query("SELECT mr FROM MissingReport mr " +
            "WHERE mr.deletedAt IS NULL " +
            "AND mr.user.id = :userId " +
            "ORDER BY mr.createdAt DESC")
    Page<MissingReport> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    // 특정 범위 내의 활성화된 실종 신고 조회 (AI 매칭용)
    @Query("SELECT mr FROM MissingReport mr " +
            "WHERE mr.deletedAt IS NULL " +
            "AND mr.status = 'MISSING' " +
            "AND mr.latitude BETWEEN :minLat AND :maxLat " +
            "AND mr.longitude BETWEEN :minLng AND :maxLng " +
            "AND mr.missingAt >= :afterDate " +
            "ORDER BY mr.missingAt DESC")
    List<MissingReport> findActiveReportsInRange(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng,
            @Param("afterDate") LocalDateTime afterDate
    );

    // MissingReportRepository
    @Query("select p.breed from MissingReport mr join mr.pet p where mr.id = :id")
    Optional<Breed> findPetBreedByReportId(Long id);

    // 최근 실종 신고 통계
    @Query("SELECT COUNT(mr) FROM MissingReport mr " +
            "WHERE mr.deletedAt IS NULL " +
            "AND mr.createdAt >= :startDate")
    Long countRecentReports(@Param("startDate") LocalDateTime startDate);
}