
package com.minjeok4go.petplace.feed.dto;
import com.minjeok4go.petplace.comment.dto.FeedComment;
import com.minjeok4go.petplace.common.constant.FeedCategory;
import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.like.repository.LikeRepository;
import com.minjeok4go.petplace.user.entity.User;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeedListResponse extends FeedDetailResponse {
    private Double score;
    // FeedListResponse.java (DTO에 추가)
    public static FeedListResponse from(Feed feed, Double score) {
        return FeedListResponse.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userId(feed.getUserId())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .regionId(feed.getRegionId())
                .category(feed.getCategory().name())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .likes(feed.getLikes())
                .views(feed.getViews())
                // ... 필요한 필드 추가
                .score(score)
                .build();
    }
    public static FeedListResponse from(Feed feed,
                                        double score,
                                        ImageRepository imageRepository,
                                        LikeRepository likeRepository,
                                        User currentUser) {
        // 태그
        List<TagResponse> tags = feed.getFeedTags().stream()
                .map(ft -> new TagResponse(ft.getTag().getId(), ft.getTag().getName()))
                .distinct()
                .toList();

        // 이미지
        List<ImageResponse> images = imageRepository
                .findByRefTypeAndRefIdOrderBySortAsc(ImageType.FEED, feed.getId())
                .stream()
                .map(img -> new ImageResponse(img.getSrc(), img.getSort()))
                .toList();

        // 댓글
        List<FeedComment> comments = feed.getComments().stream()
                .map(FeedComment::from)
                .toList();

        return FeedListResponse.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userId(feed.getUserId())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .regionId(feed.getRegionId())
                .category(feed.getCategory().getDisplayName()) // .name() 대신 보기 좋은 이름
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .liked(likeRepository.existsByFeedAndUser(feed, currentUser))
                .likes(feed.getLikes())
                .views(feed.getViews())
                .score(score)
                .tags(tags)
                .images(images)
                .comments(comments)
                .commentCount(comments.size())
                .build();
    }

}