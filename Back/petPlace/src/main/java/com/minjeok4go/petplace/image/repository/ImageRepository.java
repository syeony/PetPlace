package com.minjeok4go.petplace.image.repository;

import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {



    // ✅ 추가: 다건 배치 로딩 (정렬 포함)
    List<Image> findAllByRefTypeAndRefIdInOrderBySortAsc(RefType refType, List<Long> refIds);
    List<Image> findByRefTypeAndRefIdOrderBySortAsc(RefType refType, Long refId);
    Optional<Image> findByRefTypeAndRefIdAndSort(RefType refType, Long refId, Integer sort);

    @Query("select i from Image i where i.id in :ids")
    List<Image> findAllByIdIn(@Param("ids") List<Long> ids);
    @Modifying
    @Query("delete from Image i where i.refType = :type and i.refId = :refId")
    void deleteAllByRef(@Param("type") RefType type, @Param("refId") Long refId);
}
