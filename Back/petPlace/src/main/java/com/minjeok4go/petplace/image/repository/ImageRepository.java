package com.minjeok4go.petplace.image.repository;

import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByRefTypeAndRefIdOrderBySortAsc(ImageType refType, Long refId); // 기존 단건

    Optional<Image> findByRefTypeAndRefIdAndSort(ImageType refType, Long refId, Integer sort); // 기존 단건

    // ✅ 추가: 다건 배치 로딩 (정렬 포함)
    List<Image> findAllByRefTypeAndRefIdInOrderBySortAsc(ImageType refType, List<Long> refIds);
}
