package com.minjeok4go.petplace.comment.service;

import com.minjeok4go.petplace.comment.dto.CreateCommentRequest;
import com.minjeok4go.petplace.comment.dto.FeedComment;
import com.minjeok4go.petplace.comment.dto.DeleteCommentResponse;
import com.minjeok4go.petplace.comment.dto.MyComment;
import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.comment.repository.CommentRepository;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedService feedService;

    @Transactional(readOnly = true)
    public MyComment getCommentDetail(Long id) {
        Comment comment = commentRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + id));
        return mapComment(comment);
    }

    @Transactional(readOnly = true)
    public List<FeedComment> getCommentsByFeed(Long feedId) {
        // feed 존재 여부는 전제되었으니 생략 가능
        return commentRepository
                .findByFeedIdAndDeletedAtIsNull(feedId)
                .stream()
                .filter(c -> c.getParentComment() == null)
                .map(this::mapCommentWithReplies)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Integer getCommentCountByFeed(Long feedId) {
        // feed 존재 여부는 전제되었으니 생략 가능
        List<Comment> comments = commentRepository.findByFeedIdAndDeletedAtIsNull(feedId);
        return comments.size();
    }

    @Transactional(readOnly = true)
    public List<MyComment> getCommentsByUser(Long userId) {
        return commentRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(this::mapComment)
                .collect(Collectors.toList());
    }

    @Transactional
    public MyComment createComment(CreateCommentRequest req, User me) {
        // 1) Feed가 실제로 존재하는지 확인
        Feed feed = feedService.findById(req.getFeedId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Feed not found with id " + req.getFeedId()));

        // 2) 부모 댓글이 주어졌으면, 실제로 DB에서 조회
        Comment parent = null;
        if (req.getParentCommentId() != null) {
            parent = commentRepository
                    .findByIdAndDeletedAtIsNull(req.getParentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent comment not found with id " + req.getParentCommentId()));
        }

        // 3) Comment 빌드 + 저장
        Comment comment = Comment.builder()
                .feed(feed)
                .parentComment(parent)
                .content(req.getContent())
                .userId(me.getId())
                .userNick(me.getNickname())
                .userImg(me.getUserImgSrc())
                .build();

        Comment saved = commentRepository.save(comment);
        return mapComment(saved);
    }

    @Transactional
    public DeleteCommentResponse deleteComment(Long id, User me) {
        Comment comment = commentRepository
                .findByIdAndUserIdAndDeletedAtIsNull(id, me.getId())
                .orElseThrow(() -> new AccessDeniedException("본인 댓글이 아니거나 존재하지 않습니다"));

        softDeleteRecursively(comment);

        return new DeleteCommentResponse(id);
    }

    private void softDeleteRecursively(Comment comment) {
        comment.delete();

        commentRepository
                .findByParentCommentIdAndDeletedAtIsNull(comment.getId())
                .forEach(this::softDeleteRecursively);
    }

    private MyComment mapComment(Comment comment) {
        Long parentId = comment.getParentComment() != null
                ? comment.getParentComment().getId()
                : null;

        return FeedComment.builder()
                .id(comment.getId())
                .parentCommentId(parentId)
                .feedId(comment.getFeed().getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .userNick(comment.getUserNick())
                .userImg(comment.getUserImg())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .deletedAt(comment.getDeletedAt())
                .build();
    }

    private FeedComment mapCommentWithReplies(Comment comment) {
        List<FeedComment> replyDtos = comment.getReplies().stream()
                .map(this::mapCommentWithReplies)
                .toList();

        return FeedComment.builder()
                .id(comment.getId())
                .parentCommentId(comment.getParentComment() != null
                        ? comment.getParentComment().getId()
                        : null)
                .feedId(comment.getFeed().getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .userNick(comment.getUserNick())
                .userImg(comment.getUserImg())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .deletedAt(comment.getDeletedAt())
                .replies(replyDtos)
                .build();
    }
}
