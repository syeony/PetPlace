package com.minjeok4go.petplace.feed.service;

import com.minjeok4go.petplace.comment.dto.CommentDto;
import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.feed.dto.TagDto;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.dto.FeedDetailDto;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;

    @Transactional(readOnly = true)
    public FeedDetailDto getFeedDetail(Long feedId) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        // ✅ Tag 매핑 (feedTags → TagDto)
        List<TagDto> tagDtos = feed.getFeedTags().stream()
                .map(ft -> new TagDto(ft.getTag().getId(), ft.getTag().getName()))
                .toList();

        // ✅ 상위 댓글만 필터링 (parentComment == null)
        List<CommentDto> commentDtos = feed.getComments().stream()
                .filter(c -> c.getParentComment() == null)
                .map(this::mapCommentWithReplies)
                .toList();

        return FeedDetailDto.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userId(feed.getUserId())            // ✅ uid → userId
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .regionId(feed.getRegionId())        // ✅ rid → regionId
                .category(feed.getCategory())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .likes(feed.getLikes())              // ✅ like → likes
                .views(feed.getViews())              // ✅ view → views
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
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null) // ✅ cid → parentCommentId
                .feedId(comment.getFeed().getId())       // ✅ fid → feedId
                .content(comment.getContent())
                .userId(comment.getUserId())             // ✅ uid → userId
                .userNick(comment.getUserNick())
                .userImg(comment.getUserImg())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .deletedAt(comment.getDeletedAt())
                .replies(replyDtos)
                .build();
    }
}
