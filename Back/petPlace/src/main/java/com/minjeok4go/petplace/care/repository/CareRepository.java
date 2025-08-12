package com.minjeok4go.petplace.care.repository;

import com.minjeok4go.petplace.care.entity.Cares;
import com.minjeok4go.petplace.care.entity.Cares.CareCategory;
import com.minjeok4go.petplace.care.entity.Cares.CareStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareRepository extends JpaRepository<Cares, Long> {

    /**
     * 삭제되지 않은 돌봄/산책 요청 조회
     */
    @Query("SELECT c FROM Cares c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Cares> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * 지역별 돌봄/산책 요청 목록 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT c FROM Cares c WHERE c.region.id = :regionId AND c.deletedAt IS NULL")
    Page<Cares> findByRegionIdAndNotDeleted(@Param("regionId") Long regionId, Pageable pageable);

    /**
     * 카테고리별 돌봄/산책 요청 목록 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT c FROM Cares c WHERE c.region.id = :regionId AND c.category = :category AND c.deletedAt IS NULL")
    Page<Cares> findByRegionIdAndCategoryAndNotDeleted(@Param("regionId") Long regionId,
                                                       @Param("category") CareCategory category,
                                                       Pageable pageable);

    /**
     * 상태별 돌봄/산책 요청 목록 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT c FROM Cares c WHERE c.region.id = :regionId AND c.status = :status AND c.deletedAt IS NULL")
    Page<Cares> findByRegionIdAndStatusAndNotDeleted(@Param("regionId") Long regionId,
                                                     @Param("status") CareStatus status,
                                                     Pageable pageable);

    /**
     * 사용자별 돌봄/산책 요청 목록 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT c FROM Cares c WHERE c.user.id = :userId AND c.deletedAt IS NULL")
    Page<Cares> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    /**
     * 키워드로 돌봄/산책 요청 검색 (제목, 내용)
     */
    @Query("SELECT c FROM Cares c WHERE c.region.id = :regionId " +
           "AND (c.title LIKE %:keyword% OR c.content LIKE %:keyword%) " +
           "AND c.deletedAt IS NULL")
    Page<Cares> findByRegionIdAndKeywordAndNotDeleted(@Param("regionId") Long regionId,
                                                      @Param("keyword") String keyword,
                                                      Pageable pageable);

    /**
     * 반려동물 종류별 돌봄/산책 요청 목록 조회
     */
    @Query("SELECT c FROM Cares c WHERE c.region.id = :regionId " +
           "AND c.pet.animalType = :animalType AND c.deletedAt IS NULL")
    Page<Cares> findByRegionIdAndAnimalTypeAndNotDeleted(@Param("regionId") Long regionId,
                                                         @Param("animalType") String animalType,
                                                         Pageable pageable);

    /**
     * 기간별 돌봄/산책 요청 목록 조회 (시작일 기준)
     */
    @Query("SELECT c FROM Cares c WHERE c.region.id = :regionId " +
           "AND c.startDatetime BETWEEN :startDate AND :endDate " +
           "AND c.deletedAt IS NULL")
    Page<Cares> findByRegionIdAndDateRangeAndNotDeleted(@Param("regionId") Long regionId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        Pageable pageable);

    /**
     * 활성 상태인 돌봄/산책 요청 수 조회 (지역별)
     */
    @Query("SELECT COUNT(c) FROM Cares c WHERE c.region.id = :regionId " +
           "AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    Long countActiveByRegionId(@Param("regionId") Long regionId);

    /**
     * 사용자가 등록한 활성 상태인 돌봄/산책 요청 목록
     */
    @Query("SELECT c FROM Cares c WHERE c.user.id = :userId " +
           "AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    List<Cares> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 곧 시작될 돌봄/산책 요청 목록 조회 (알림용)
     */
    @Query("SELECT c FROM Cares c WHERE c.startDatetime BETWEEN :now AND :oneHourLater " +
           "AND c.status = 'MATCHED' AND c.deletedAt IS NULL")
    List<Cares> findUpcomingMatched(@Param("now") LocalDateTime now,
                                   @Param("oneHourLater") LocalDateTime oneHourLater);

    /**
     * 복합 조건 검색 (카테고리, 상태, 키워드)
     */
    @Query("SELECT c FROM Cares c WHERE c.region.id = :regionId " +
           "AND (:category IS NULL OR c.category = :category) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:keyword IS NULL OR c.title LIKE %:keyword% OR c.content LIKE %:keyword%) " +
           "AND c.deletedAt IS NULL")
    Page<Cares> findByComplexConditionsAndNotDeleted(@Param("regionId") Long regionId,
                                                     @Param("category") CareCategory category,
                                                     @Param("status") CareStatus status,
                                                     @Param("keyword") String keyword,
                                                     Pageable pageable);
}
