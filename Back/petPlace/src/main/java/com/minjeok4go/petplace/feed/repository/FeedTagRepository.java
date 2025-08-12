package com.minjeok4go.petplace.feed.repository;

import com.minjeok4go.petplace.feed.entity.FeedTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FeedTagRepository extends JpaRepository<FeedTag, Long> {
    List<FeedTag> findByFeedId(Long feedId);
    List<FeedTag> findByTagId(Long tagId);

    @Query("select ft.tag.id from FeedTag ft where ft.feed.id = :feedId")
    List<Long> findTagIdByFeedId(Long feedId);

    @Query("select ft.tag.id from FeedTag ft where ft.feed.id = :feedId")
    List<Long> findTagIdsByFeedId(@Param("feedId") Long feedId);

    @Modifying
    @Query("delete from FeedTag ft where ft.feed.id = :feedId and ft.tag.id in :tagIds")
    void deleteByFeedIdAndTagIdIn(@Param("feedId") Long feedId, @Param("tagIds") Collection<Long> tagIds);
}
