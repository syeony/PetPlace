package com.minjeok4go.petplace.comment.repository;

import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
    Optional<Comment> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
    List<Comment> findByFeedIdAndDeletedAtIsNull(Long feedId);
    List<Comment> findByUserIdAndDeletedAtIsNull(Long userId);
    List<Comment> findByParentCommentIdAndDeletedAtIsNull(Long parentCommentId);
    // 댓글 개수 카운트를 메서드로 추가
    int countByFeedIdAndDeletedAtIsNull(Long feedId);

    @Query("SELECT c.feed.id, COUNT(c.id) FROM Comment c WHERE c.feed.id IN :feedIds AND c.deletedAt IS NULL GROUP BY c.feed.id")
    List<Object[]> countByFeedIdInAndDeletedAtIsNullGroupByFeedId(@Param("feedIds") List<Long> feedIds);

    @Query("select distinct c.feed.id from Comment c where c.userId = :uid and c.deletedAt is null")
    List<Long> findFeedIdsByUserId(@Param("uid") Long userId);

    List<Comment> findByParentCommentIdAndDeletedAtIsNullOrderByIdAsc(Long parentCommentId);
    List<Comment> findByFeedAndDeletedAtIsNullOrderByIdAsc(Feed feed);
}
