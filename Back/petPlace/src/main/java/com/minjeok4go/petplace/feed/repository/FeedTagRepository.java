package com.minjeok4go.petplace.feed.repository;

import com.minjeok4go.petplace.feed.entity.FeedTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FeedTagRepository extends JpaRepository<FeedTag, Long> {

    interface FeedTagPair {
        Long getFeedId();
        Long getTagId();
    }

    List<FeedTag> findByFeedId(Long feedId);
    List<FeedTag> findByTagId(Long tagId);
    List<Long> findTagIdByFeedId(Long feedId);

    // 후보 피드들에 대한 (feedId, tagId) 일괄 조회
    @Query("select ft.feed.id as feedId, ft.tag.id as tagId from FeedTag ft where ft.feed.id in :feedIds")
    List<FeedTagPair> findFeedTagPairsByFeedIdIn(@Param("feedIds") Collection<Long> feedIds);
}

