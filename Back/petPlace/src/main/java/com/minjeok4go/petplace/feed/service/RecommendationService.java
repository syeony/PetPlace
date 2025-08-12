//package com.minjeok4go.petplace.feed.service;
//
//import com.minjeok4go.petplace.comment.dto.FeedComment;
//import com.minjeok4go.petplace.common.constant.ImageType;
//import com.minjeok4go.petplace.feed.dto.FeedListResponse;
//import com.minjeok4go.petplace.feed.dto.TagResponse;
//import com.minjeok4go.petplace.feed.entity.Feed;
//import com.minjeok4go.petplace.feed.repository.FeedRepository;
//import com.minjeok4go.petplace.image.dto.ImageResponse;
//import com.minjeok4go.petplace.image.repository.ImageRepository;
//import com.minjeok4go.petplace.like.repository.LikeRepository;
//import com.minjeok4go.petplace.like.service.LikeService;
//import com.minjeok4go.petplace.user.entity.User;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class RecommendationService {
//
//    private final FeedRepository feedRepository;
//    private final ImageRepository imageRepository;
//    private final LikeRepository likeRepository;
//
//    @Transactional(readOnly = true)
//    public List<FeedListResponse> getRecommendedFeeds(User user, int page, int size) {
//        Long userId = user.getId();
//        boolean isColdStart = checkColdStart(userId);
//
//        List<Feed> allFeeds = feedRepository.findAllByDeletedAtIsNull();
//
//        List<FeedListResponse> scoredFeedDtos = allFeeds.stream()
//                .map(feed -> {
//                    double contentScore = calcContentScore(userId, feed);
//                    double cfScore = isColdStart ? 0.0 : calcCollaborativeScore(userId, feed);
//
//                    double hybridScore;
//                    if (isColdStart) {
//                        double popularScore = getPopularScore(feed);
//                        hybridScore = 0.8 * contentScore + 0.2 * popularScore;
//                    } else {
//                        hybridScore = 0.6 * contentScore + 0.4 * cfScore;
//                    }
//
//                    // --- DTO 조립 시작 ---
//                    // 1) TagDto 리스트
//                    List<TagResponse> tags = feed.getFeedTags().stream()
//                            .map(ft -> new TagResponse(ft.getTag().getId(), ft.getTag().getName()))
//                            .distinct()
//                            .collect(Collectors.toList());
//
//                    // 2) ImageResponse 리스트
//                    List<ImageResponse> images = imageRepository
//                            .findByRefTypeAndRefIdOrderBySortAsc(ImageType.FEED, feed.getId())
//                            .stream()
//                            .map(img -> new ImageResponse(img.getId(), img.getSrc(), img.getSort()))
//                            .collect(Collectors.toList());
//
//                    // 3) CommentDto 리스트
//                    List<FeedComment> comments = feed.getComments().stream()
//                            .map(FeedComment::from)
//                            .collect(Collectors.toList());
//
//                    // 4) 최종 FeedDto 빌드
//                    return FeedListResponse.builder()
//                            .id(feed.getId())
//                            .content(feed.getContent())
//                            .userId(feed.getUserId())
//                            .userNick(feed.getUserNick())
//                            .userImg(feed.getUserImg())
//                            .regionId(feed.getRegionId())
//                            .category(feed.getCategory().getDisplayName())
//                            .createdAt(feed.getCreatedAt())
//                            .updatedAt(feed.getUpdatedAt())
//                            .deletedAt(feed.getDeletedAt())
//                            .liked(likeRepository.existsByFeedAndUser(feed, user))
//                            .likes(feed.getLikes())
//                            .views(feed.getViews())
//                            .score(hybridScore)
//                            .tags(tags)
//                            .images(images)
//                            .comments(comments)
//                            .commentCount(comments.size())
//                            .build();
//                    // --- DTO 조립 끝 ---
//                })
//                .sorted(Comparator.comparingDouble(FeedListResponse::getScore).reversed())
//                .collect(Collectors.toList());
//
//        // 페이지네이션 적용
//        int fromIdx = page * size;
//        int toIdx = Math.min(fromIdx + size, scoredFeedDtos.size());
//        if (fromIdx >= toIdx) {
//            return Collections.emptyList();
//        }
//        return scoredFeedDtos.subList(fromIdx, toIdx);
//    }
//
//    private boolean checkColdStart(Long userId) {
//        int userActionCount = getUserActionCount(userId);
//        return userActionCount < 3;
//    }
//
//    private int getUserActionCount(Long userId) {
//        // TODO: DB 기반 좋아요/댓글 카운트 조회
//        return 0;
//    }
//
//    private double calcContentScore(Long userId, Feed feed) {
//        List<Long> userFavTags = List.of(1L, 3L, 7L);
//        Set<Long> feedTags = feed.getFeedTags().stream()
//                .map(ft -> ft.getTag().getId())
//                .collect(Collectors.toSet());
//        long matchCount = userFavTags.stream().filter(feedTags::contains).count();
//        double tagScore = matchCount / (double) userFavTags.size();
//
//        String userFavCategory = "MYPET";
//        boolean categoryMatch = userFavCategory.equals(feed.getCategory().name());
//        double categoryScore = categoryMatch ? 1.0 : 0.0;
//
//        return 0.7 * tagScore + 0.3 * categoryScore;
//    }
//
//    private double calcCollaborativeScore(Long userId, Feed feed) {
//        List<Long> likedTags = List.of(2L, 3L, 6L);
//        Set<Long> feedTags = feed.getFeedTags().stream()
//                .map(ft -> ft.getTag().getId())
//                .collect(Collectors.toSet());
//        long matchCount = likedTags.stream().filter(feedTags::contains).count();
//        return matchCount / (double) likedTags.size();
//    }
//
//    private double getPopularScore(Feed feed) {
//        int maxView = 500;
//        int maxLike = 30;
//
//        double viewScore = (double) feed.getViews() / maxView;
//        double likeScore = (double) feed.getLikes() / maxLike;
//
//        return 0.7 * likeScore + 0.3 * viewScore;
//    }
//}
