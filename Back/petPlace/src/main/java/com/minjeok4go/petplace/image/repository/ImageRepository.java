package com.minjeok4go.petplace.image.repository;

import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByRefTypeAndRefIdOrderBySortAsc(ImageType refType, Long refId);
    Optional<Image> findByRefTypeAndRefIdAndSort(ImageType refType, Long refId, Integer sort);
}
