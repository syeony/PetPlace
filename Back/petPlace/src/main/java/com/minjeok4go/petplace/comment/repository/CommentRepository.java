package com.minjeok4go.petplace.comment.repository;

import com.minjeok4go.petplace.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
    Optional<Comment> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
    List<Comment> findByFeedIdAndDeletedAtIsNull(Long feedId);
    List<Comment> findByUserIdAndDeletedAtIsNull(Long userId);
    List<Comment> findByParentCommentIdAndDeletedAtIsNull(Long parentCommentId);
}
