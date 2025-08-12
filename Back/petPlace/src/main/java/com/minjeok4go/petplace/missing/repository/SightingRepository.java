package com.minjeok4go.petplace.missing.repository;

import com.minjeok4go.petplace.missing.entity.Sighting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SightingRepository extends JpaRepository<Sighting, Long> {

    // 삭제되지 않은 목격 제보 조회
    @Query("SELECT s FROM Sighting s WHERE s.deletedAt IS NULL AND s.id = :id")
    Optional<Sighting> findByIdAndNotDeleted(@Param("id") Long id);

    // 지역별 목격 제보 목록 조회 (페이징)
    @Query("SELECT s FROM Sighting s " +
            "WHERE s.deletedAt IS NULL " +
            "AND s.region.id = :regionId " +
            "ORDER BY s.createdAt DESC")
    Page<Sighting> findByRegionIdAndNotDeleted(@Param("regionId") Long regionId, Pageable pageable);

    // 사용자별 목격 제보 목록 조회
    @Query("SELECT s FROM Sighting s " +
            "WHERE s.deletedAt IS NULL " +
            "AND s.user.id = :userId " +
            "ORDER BY s.createdAt DESC")
    Page<Sighting> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    // 최근 목격 제보 통계
    @Query("SELECT COUNT(s) FROM Sighting s " +
            "WHERE s.deletedAt IS NULL " +
            "AND s.createdAt >= :startDate")
    Long countRecentSightings(@Param("startDate") LocalDateTime startDate);
}