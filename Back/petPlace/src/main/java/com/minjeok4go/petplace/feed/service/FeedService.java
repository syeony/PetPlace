package com.minjeok4go.petplace.feed.service;

import com.minjeok4go.petplace.comment.dto.CommentDto;
import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.feed.dto.FeedDetailDto;
import com.minjeok4go.petplace.feed.dto.TagDto;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public FeedDetailDto getFeedDetail(Long feedId) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        List<TagDto> tagDtos = feed.getFeedTags().stream()
                .map(ft -> new TagDto(ft.getTag().getId(), ft.getTag().getName()))
                .toList();

        List<CommentDto> commentDtos = feed.getComments().stream()
                .filter(c -> c.getParentComment() == null)
                .map(this::mapCommentWithReplies)
                .toList();

        List<ImageResponse> imageDtos = imageRepository
                .findByRefTypeAndRefIdOrderBySortAsc(ImageType.FEED, feedId)
                .stream()
                .map(img -> new ImageResponse(img.getSrc(), img.getSort()))
                .collect(Collectors.toList());

        return FeedDetailDto.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userId(feed.getUserId())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .regionId(feed.getRegionId())
                .category(feed.getCategory())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .likes(feed.getLikes())
                .views(feed.getViews())
                .tags(tagDtos)
                .images(imageDtos)
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
