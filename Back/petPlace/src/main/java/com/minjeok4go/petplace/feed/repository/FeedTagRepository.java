package com.minjeok4go.petplace.feed.repository;

import com.minjeok4go.petplace.feed.entity.FeedTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedTagRepository extends JpaRepository<FeedTag, Long> {
    List<FeedTag> findByFeedId(Long feedId);
}
