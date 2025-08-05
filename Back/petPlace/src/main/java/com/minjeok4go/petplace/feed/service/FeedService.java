package com.minjeok4go.petplace.feed.service;

import com.minjeok4go.petplace.comment.dto.CommentDto;
import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.common.constant.FeedCategory;
import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.feed.dto.CreateFeedRequest;
import com.minjeok4go.petplace.feed.dto.DeleteFeedResponse;
import com.minjeok4go.petplace.feed.dto.FeedDetailResponse;
import com.minjeok4go.petplace.feed.dto.TagResponse;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.entity.FeedTag;
import com.minjeok4go.petplace.feed.entity.Tag;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.feed.repository.FeedTagRepository;
import com.minjeok4go.petplace.feed.repository.TagRepository;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedTagRepository feedTagRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public FeedDetailResponse getFeedDetail(Long feedId) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        List<TagResponse> tagDtos = feed.getFeedTags().stream()
                .map(ft -> new TagResponse(ft.getTag().getId(), ft.getTag().getName()))
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

        return FeedDetailResponse.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userId(feed.getUserId())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .regionId(feed.getRegionId())
                .category(feed.getCategory().getDisplayName())
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
    @Transactional
    public FeedDetailResponse createFeed(CreateFeedRequest req, User user) {
        // 1) 피드 저장
        Feed feed = Feed.builder()
                .content(req.getContent())
                .userId(user.getId().longValue())
                .userNick(user.getNickname())
                .regionId(req.getRegionId())
                .category(FeedCategory.valueOf(req.getCategory()))
                .build();
        feed = feedRepository.save(feed);

        // 2) FeedTag / Image 삽입
        saveRelations(feed, req);

        return getFeedDetail(feed.getId());
    }

    @Transactional
    public FeedDetailResponse updateFeed(Long id, CreateFeedRequest req, User user) {
        // 1) 한 번에 조회 + 권한검사
        Feed feed = feedRepository
                .findByIdAndUserId(id, user.getId().longValue())
                .orElseThrow(() -> new AccessDeniedException("해당 피드를 찾을 수 없거나, 삭제 권한이 없습니다."));

        // 2) 실제 수정
        feed.setContent(req.getContent());
        feed.setUserNick(user.getNickname());
        feed.setRegionId(req.getRegionId());
        feed.setCategory(FeedCategory.valueOf(req.getCategory()));
        feed.update();
        feedRepository.save(feed);

        // 3) 관계삽입
        saveRelations(feed, req);

        return getFeedDetail(feed.getId());
    }

    @Transactional
    public DeleteFeedResponse deleteFeed(Long id, User user) {
        Feed feed = feedRepository
                .findByIdAndUserId(id, user.getId().longValue())
                .orElseThrow(() -> new AccessDeniedException("해당 피드를 찾을 수 없거나, 삭제 권한이 없습니다."));

        feed.delete();
        feedRepository.save(feed);
        return new DeleteFeedResponse(id);
    }

    private void saveRelations(Feed feed, CreateFeedRequest req) {
        Long feedId = feed.getId();

        // --- tags
        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            // 1) 요청된 태그 리스트에서 중복 제거
            List<Long> requested = req.getTagIds().stream()
                    .distinct()
                    .toList();

            // 2) DB에 이미 저장된 tagId 목록 조회
            List<Long> existing = feedTagRepository.findByFeedId(feedId).stream()
                    .map(ft -> ft.getTag().getId())
                    .toList();

            // 3) 새로 추가할 tagId = requested – existing
            List<Long> toAdd = requested.stream()
                    .filter(id -> !existing.contains(id))
                    .toList();

            // 4) 나머지에 대해서만 insert
            List<FeedTag> feedTags = toAdd.stream()
                    .map(tagId -> {
                        Tag tag = tagRepository.findById(tagId)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid tag ID: " + tagId));
                        return new FeedTag(feed, tag);
                    })
                    .toList();
            feedTagRepository.saveAll(feedTags);
        }

        // --- images
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            // 요청된 이미지 src+sort 조합 중복 제거
            List<ImageRequest> requested = req.getImages().stream()
                    .distinct()
                    .toList();

            // 이미 DB에 저장된 이미지들의 (src, sort) 키 조회
            List<Image> existImgs = imageRepository.findByRefTypeAndRefIdOrderBySortAsc(ImageType.FEED, feedId);
            List<String> existKeys = existImgs.stream()
                    .map(img -> img.getSrc() + "#" + img.getSort())
                    .toList();

            // toAdd 는 존재하지 않는 키만
            List<Image> toAdd = requested.stream()
                    .filter(ir -> !existKeys.contains(ir.getSrc() + "#" + ir.getSort()))
                    .map(ir -> new Image(feedId, ImageType.FEED, ir.getSrc(), ir.getSort()))
                    .toList();

            imageRepository.saveAll(toAdd);
        }
    }
}
