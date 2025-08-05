package com.minjeok4go.petplace.feed.repository;

import com.minjeok4go.petplace.feed.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
