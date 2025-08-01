package com.minjeok4go.petplace.domain.feed.service;

import com.minjeok4go.petplace.domain.feed.model.*;
import com.minjeok4go.petplace.domain.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor

public class FeedService {
    private final FeedRepository feedRepository;
    @Transactional(readOnly = true)
    public FeedDetailDto getFeedDetail(Long feedId) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        // Tag 매핑
        List<TagDto> tagDtos = feed.getHashtags().stream()
                .map(h -> new TagDto(h.getTag().getId(), h.getTag().getTagName()))
                .toList();

        // 상위 댓글만 필터링 (대댓글 제외)
        List<CommentDto> commentDtos = feed.getComments().stream()
                .filter(c -> c.getComment() == null)
                .map(this::mapCommentWithReplies)
                .toList();

        return FeedDetailDto.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .uid(feed.getUid())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .rid(feed.getRid())
                .category(feed.getCategory())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .like(feed.getLike())
                .view(feed.getView())
                .tags(tagDtos)
                .commentCount(feed.getComments().size())
                .comments(commentDtos)
                .build();
    }

    private CommentDto mapCommentWithReplies(Comment comment) {
        List<CommentDto> replyDtos = comment.getReplies().stream()
                .map(this::mapCommentWithReplies)
                .toList();

        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .uid(comment.getUid())
                .userNick(comment.getUserNick())
                .userImg(comment.getUserImg())
                .createdAt(comment.getCreatedAt())
                .replies(replyDtos)
                .build();
    }
}
