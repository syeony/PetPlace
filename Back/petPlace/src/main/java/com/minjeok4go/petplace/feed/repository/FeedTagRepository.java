package com.minjeok4go.petplace.feed.repository;

import com.minjeok4go.petplace.feed.dto.FeedTagJoin;
import com.minjeok4go.petplace.feed.entity.FeedTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("select ft.tag.id from FeedTag ft where ft.feed.id = :feedId")
    List<Long> findTagIdByFeedId(Long feedId);

    // 후보 피드들에 대한 (feedId, tagId) 일괄 조회
    @Query("select ft.feed.id as feedId, ft.tag.id as tagId from FeedTag ft where ft.feed.id in :feedIds")
    List<FeedTagPair> findFeedTagPairsByFeedIdIn(@Param("feedIds") Collection<Long> feedIds);

    @Query("""
        select new com.minjeok4go.petplace.feed.dto.FeedTagJoin(ft.feed.id, t.id, t.name)
        from FeedTag ft join ft.tag t
        where ft.feed.id in :ids
        """)
    List<FeedTagJoin> findAllByFeedIdIn(@Param("ids") List<Long> ids);

    @Query("select ft.tag.id from FeedTag ft where ft.feed.id = :feedId")
    List<Long> findTagIdsByFeedId(@Param("feedId") Long feedId);

    @Modifying
    @Query("delete from FeedTag ft where ft.feed.id = :feedId and ft.tag.id in :tagIds")
    void deleteByFeedIdAndTagIdIn(@Param("feedId") Long feedId, @Param("tagIds") Collection<Long> tagIds);
}

