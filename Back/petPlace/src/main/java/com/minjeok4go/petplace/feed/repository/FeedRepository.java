package com.minjeok4go.petplace.feed.repository;

import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.common.constant.FeedCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<Feed, Long> {

//     ✅ Feed + Tag + Comment를 Fetch Join으로 가져오기
//    @Query("SELECT DISTINCT f FROM Feed f " +
//            "LEFT JOIN FETCH f.feedTags ft " +
//            "LEFT JOIN FETCH ft.tag " +
//            "LEFT JOIN FETCH f.comments c " +
//            "LEFT JOIN FETCH c.replies " +
//            "WHERE f.id = :feedId")
//    Optional<Feed> findFeedWithTagsAndComments(@Param("feedId") Long feedId);
    @Query("SELECT f FROM Likes l " +
           "JOIN l.feed f " +
           "WHERE l.user.id = :userId " +
           "AND f.deletedAt IS NULL")
    List<Feed> findLikedFeedsByUserId(Long userId);

    Optional<Feed> findByIdAndDeletedAtIsNull(Long id);

    List<Feed> findAllByDeletedAtIsNull();

    Optional<Feed> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    List<Feed> findByCategory(FeedCategory category);

    // ✅ feedTags로 경로 변경
    @EntityGraph(attributePaths = {"feedTags.tag"})
    List<Feed> findDistinctByFeedTags_Tag_Id(Long tagId);

    List<Feed> findByUserNick(String userNick);
}
