package com.minjeok4go.petplace.feed.repository;

import com.minjeok4go.petplace.feed.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOrderByIdAsc();

    List<Tag> findByIdIn(Collection<Long> id);
}
