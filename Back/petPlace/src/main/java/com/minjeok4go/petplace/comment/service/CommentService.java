package com.minjeok4go.petplace.comment.service;

import com.minjeok4go.petplace.comment.dto.CreateCommentRequest;
import com.minjeok4go.petplace.comment.dto.FeedComment;
import com.minjeok4go.petplace.comment.dto.DeleteCommentResponse;
import com.minjeok4go.petplace.comment.dto.MyComment;
import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.comment.repository.CommentRepository;
import com.minjeok4go.petplace.common.constant.ActivityType;
import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.notification.dto.CreateCommentNotificationRequest;
import com.minjeok4go.petplace.notification.dto.CreateReplyNotificationRequest;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.UserExperienceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final ApplicationEventPublisher publisher;
    private final CommentRepository commentRepository;
    private final FeedService feedService;
    private final UserExperienceService expService;

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

        expService.applyActivity(me, ActivityType.COMMENT_CREATE);

        Long feedOwnerId = feed.getUserId();
        if (parent == null) {
            // 일반 댓글
            if (!Objects.equals(feedOwnerId, me.getId())) {
                publisher.publishEvent(new CreateCommentNotificationRequest(
                        feedOwnerId, me.getNickname(), RefType.FEED, feed.getId(), saved.getId(), saved.getContent()
                ));
            }
        } else {
            Long parentOwnerId = parent.getUserId();

            if (Objects.equals(feedOwnerId, parentOwnerId)) {
                // 같은 사람에게 두 번 보내지 말고, '답글'로 1건만
                if (!Objects.equals(feedOwnerId, me.getId())) {
                    publisher.publishEvent(new CreateReplyNotificationRequest(
                            feedOwnerId, me.getNickname(), RefType.FEED, feed.getId(), saved.getId(), saved.getContent()
                    ));
                }
            } else {
                // 서로 다른 사람이라면 각자 1건씩
                if (!Objects.equals(feedOwnerId, me.getId())) {
                    publisher.publishEvent(new CreateCommentNotificationRequest(
                            feedOwnerId, me.getNickname(), RefType.FEED, feed.getId(), saved.getId(), saved.getContent()
                    ));
                }
                if (!Objects.equals(parentOwnerId, me.getId())) {
                    publisher.publishEvent(new CreateReplyNotificationRequest(
                            parentOwnerId, me.getNickname(), RefType.FEED, feed.getId(), saved.getId(), saved.getContent()
                    ));
                }
            }
        }


        return mapComment(saved);
    }

    @Transactional
    public DeleteCommentResponse deleteComment(Long id, User me) {
        Comment comment = commentRepository
                .findByIdAndUserIdAndDeletedAtIsNull(id, me.getId())
                .orElseThrow(() -> new AccessDeniedException("본인 댓글이 아니거나 존재하지 않습니다"));

        softDeleteRecursively(comment);

        expService.applyActivity(me, ActivityType.COMMENT_DELETE);

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

//    private FeedComment mapCommentWithReplies(Comment comment) {
//        List<FeedComment> replyDtos = comment.getReplies().stream()
//                .map(this::mapCommentWithReplies)
//                .toList();
//
//        return FeedComment.builder()
//                .id(comment.getId())
//                .parentCommentId(comment.getParentComment() != null
//                        ? comment.getParentComment().getId()
//                        : null)
//                .feedId(comment.getFeed().getId())
//                .content(comment.getContent())
//                .userId(comment.getUserId())
//                .userNick(comment.getUserNick())
//                .userImg(comment.getUserImg())
//                .createdAt(comment.getCreatedAt())
//                .updatedAt(comment.getUpdatedAt())
//                .deletedAt(comment.getDeletedAt())
//                .replies(replyDtos)
//                .build();
//    }
    private FeedComment mapCommentWithReplies(Comment comment) {
        List<Comment> activeReplies =
                commentRepository.findByParentCommentIdAndDeletedAtIsNullOrderByIdAsc(comment.getId());

        List<FeedComment> replyDtos = activeReplies.stream()
                .map(this::mapCommentWithReplies)
                .toList();

        return FeedComment.builder()
                .id(comment.getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
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
