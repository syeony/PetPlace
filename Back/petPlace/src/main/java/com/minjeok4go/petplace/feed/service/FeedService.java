package com.minjeok4go.petplace.feed.service;

import com.minjeok4go.petplace.comment.dto.FeedComment;
import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.comment.repository.CommentRepository;
import com.minjeok4go.petplace.common.constant.ActivityType;
import com.minjeok4go.petplace.common.constant.FeedCategory;
import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.feed.dto.*;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.entity.FeedTag;
import com.minjeok4go.petplace.feed.entity.Tag;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.feed.repository.FeedTagRepository;
import com.minjeok4go.petplace.feed.repository.TagRepository;
import com.minjeok4go.petplace.image.dto.FeedImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.like.repository.LikeRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.RecommendationCacheService;
import com.minjeok4go.petplace.user.service.UserExperienceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedTagRepository feedTagRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;
    private final RecommendationCacheService recommendationCacheService; // ⬅ 추가
    private final UserExperienceService expService;


    @Transactional(readOnly = true)
    public FeedDetailResponse getFeedDetail(Long feedId, User user) {

        Feed feed = feedRepository.findByIdAndDeletedAtIsNull(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        return mapFeedToDetail(feed, user);
    }

//    private FeedComment mapCommentWithRepliesFiltered(Comment comment) {
//        List<FeedComment> replyDtos = comment.getReplies().stream()
//                .filter(r -> r.getDeletedAt() == null) // 대댓글도 삭제 제외
//                .sorted(Comparator.comparing(Comment::getId)) // 또는 createdAt
//                .map(this::mapCommentWithRepliesFiltered)
//                .toList();
//
//        return new FeedComment(comment, replyDtos);
//    }
    private FeedComment mapCommentWithRepliesFiltered(Comment comment) {
        // 대댓글은 항상 쿼리로 재조회 → soft delete/동시성 즉시 반영
        List<Comment> activeReplies =
                commentRepository.findByParentCommentIdAndDeletedAtIsNullOrderByIdAsc(comment.getId());

        List<FeedComment> replyDtos = activeReplies.stream()
                .map(this::mapCommentWithRepliesFiltered)
                .toList();

        return new FeedComment(comment, replyDtos);
    }
    @Transactional
    public FeedDetailResponse createFeed(CreateFeedRequest req, User user) {
        // 1) 피드 저장
        Feed feed = new Feed(req, user);

        Feed saved = feedRepository.save(feed);

        syncTags(feed.getId(), req.getTagIds());
        syncImages(feed.getId(), req.getImages());

        expService.applyActivity(user, ActivityType.FEED_CREATE);

        return getFeedDetail(saved.getId(), user);
    }

    @Transactional
    public FeedDetailResponse updateFeed(Long id, CreateFeedRequest req, User user) {
        // 1) 한 번에 조회 + 권한검사
        Feed feed = feedRepository
                .findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AccessDeniedException("해당 피드를 찾을 수 없거나, 삭제 권한이 없습니다."));

        // 2) 실제 수정
        feed.setContent(req.getContent());
        feed.setUserNick(user.getNickname());
        feed.setUserImg(user.getUserImgSrc());
        feed.setRegionId(req.getRegionId());
        feed.setCategory(FeedCategory.valueOf(req.getCategory()));
        feed.update();

        Feed saved = feedRepository.save(feed);

        syncTags(feed.getId(), req.getTagIds());
        syncImages(feed.getId(), req.getImages());

        return getFeedDetail(saved.getId(), user);
    }

    @Transactional
    public DeleteFeedResponse deleteFeed(Long id, User user) {
        Feed feed = feedRepository
                .findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AccessDeniedException("해당 피드를 찾을 수 없거나, 삭제 권한이 없습니다."));

        feed.delete();
        feedRepository.save(feed);

        expService.applyActivity(user, ActivityType.FEED_DELETE);

        return new DeleteFeedResponse(id);
    }

    private void syncTags(Long feedId, List<Long> requestedTagIds) {
        if (requestedTagIds == null) requestedTagIds = List.of();

        Set<Long> requested = new HashSet<>(requestedTagIds);
        List<Long> existing = feedTagRepository.findTagIdsByFeedId(feedId);
        Set<Long> existingSet = new HashSet<>(existing);

        // 삭제 대상 = 기존 - 요청
        Set<Long> toDelete = new HashSet<>(existingSet);
        toDelete.removeAll(requested);
        if (!toDelete.isEmpty()) {
            feedTagRepository.deleteByFeedIdAndTagIdIn(feedId, toDelete);
        }

        // 추가 대상 = 요청 - 기존
        Set<Long> toAdd = new HashSet<>(requested);
        toAdd.removeAll(existingSet);
        if (!toAdd.isEmpty()) {
            List<Tag> tags = tagRepository.findByIdIn(toAdd);
            Map<Long, Tag> idToTag = tags.stream().collect(Collectors.toMap(Tag::getId, t -> t));
            List<FeedTag> feedTags = toAdd.stream()
                    .map(tagId -> new FeedTag(new Feed(feedId), idToTag.get(tagId))) // or use feed reference ctor
                    .toList();
            feedTagRepository.saveAll(feedTags);
        }

    }

    private void syncImages(Long feedId, List<FeedImageRequest> requested) {
        if (requested == null) requested = List.of();

        imageRepository.deleteAllByRef(RefType.FEED, feedId);

        if (!requested.isEmpty()) {
            List<Image> toAdd = requested.stream()
                    .map(ir -> new Image(feedId, RefType.FEED, ir.getSrc(), ir.getSort()))
                    .toList();
            imageRepository.saveAll(toAdd);
        }

    }

    @Transactional(readOnly = true)
    public Optional<Feed> findById(Long id) {
        return feedRepository.findById(id);
    }

    @Transactional
    public FeedLikeResponse increaseLike(Feed feed) {
        feed.increaseLikes();
        feedRepository.save(feed);


        return new FeedLikeResponse(feed.getId(),true, feed.getLikes());
    }

    @Transactional
    public FeedLikeResponse decreaseLike(Feed feed) {
        feed.decreaseLikes();
        feedRepository.save(feed);


        return new FeedLikeResponse(feed.getId(),false, feed.getLikes());
    }

    @Transactional(readOnly = true)
    public List<FeedDetailResponse> findByUserId(User user) {

        List<Feed> feeds = feedRepository.findByUserId(user.getId());

        return feeds.stream()
                .map(feed -> mapFeedToDetail(feed, user))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedDetailResponse> findByIdWhereUserId(User user) {
        List<Feed> feeds = feedRepository.findLikedFeedsByUserId(user.getId());

        return feeds.stream()
                .map(feed -> mapFeedToDetail(feed, user))
                .toList();
    }

    private FeedDetailResponse mapFeedToDetail(Feed feed, User user) {
        // tags
//        List<TagResponse> tagDtos = feed.getFeedTags().stream()
//                .map(ft -> new TagResponse(ft.getTag().getId(), ft.getTag().getName()))
//                .toList();
        // 1) tags (NULL SAFE)
        List<TagResponse> tagDtos =
                Optional.ofNullable(feed.getFeedTags())            // ★ null → empty
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(ft -> new TagResponse(ft.getTag().getId(), ft.getTag().getName()))
                        .toList();

        // 최상위 댓글 + 대댓글 트리
        List<Comment> comments = commentRepository.findByFeedAndDeletedAtIsNullOrderByIdAsc(feed);
        List<FeedComment> commentDtos = comments.stream()
                .filter(c -> c.getParentComment() == null)
                .map(this::mapCommentWithRepliesFiltered)  // 대댓글 재조회 버전
                .toList();

        // images
        List<ImageResponse> imageDtos = imageRepository
                .findByRefTypeAndRefIdOrderBySortAsc(RefType.FEED, feed.getId())
                .stream()
                .map(img -> new ImageResponse(img.getId(), img.getSrc(), img.getSort()))
                .toList();

        boolean liked = likeRepository.existsByFeedAndUser(feed, user);

        // DTO 생성 (생성자 시그니처 그대로 유지)
        FeedDetailResponse dto =
                new FeedDetailResponse(feed, liked, tagDtos, imageDtos, comments, commentDtos);

        // ✔ 실제 총 댓글 수(소프트 삭제 제외)로 덮어쓰기
        long total = commentRepository.countByFeedIdAndDeletedAtIsNull(feed.getId());
        dto.setCommentCount(Math.toIntExact(total));

        return dto;
    }



}

//    private FeedDetailResponse mapFeedToDetail(Feed feed, User user) {
//        // tags
//        List<TagResponse> tagDtos = feed.getFeedTags().stream()
//                .map(ft -> new TagResponse(ft.getTag().getId(), ft.getTag().getName()))
//                .toList();
//
//        // comments (삭제 제외 + 정렬 + 최상위만)
//        List<Comment> comments = commentRepository.findByFeedAndDeletedAtIsNullOrderByIdAsc(feed);
//        List<FeedComment> commentDtos = comments.stream()
//                .filter(c -> c.getParentComment() == null)
//                .map(this::mapCommentWithRepliesFiltered)
//                .toList();
//
//        // images
//        List<ImageResponse> imageDtos = imageRepository
//                .findByRefTypeAndRefIdOrderBySortAsc(ImageType.FEED, feed.getId())
//                .stream()
//                .map(img -> new ImageResponse(img.getId(), img.getSrc(), img.getSort()))
//                .toList();
//
//        boolean liked = likeRepository.existsByFeedAndUser(feed, user);
//
//        return new FeedDetailResponse(feed, liked, tagDtos, imageDtos, comments, commentDtos);
//    }

