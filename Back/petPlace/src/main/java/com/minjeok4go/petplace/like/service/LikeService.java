package com.minjeok4go.petplace.like.service;

import com.minjeok4go.petplace.common.constant.ActivityType;
import com.minjeok4go.petplace.feed.dto.FeedLikeResponse;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.like.dto.CreateLikeRequest;
import com.minjeok4go.petplace.like.entity.Likes;
import com.minjeok4go.petplace.like.repository.LikeRepository;
import com.minjeok4go.petplace.notification.dto.CreateLikeNotificationRequest;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.UserExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final ApplicationEventPublisher publisher;
    private final FeedRepository feedRepository;
    private final LikeRepository likeRepository;
    private final FeedService feedService;
    private final UserExperienceService expService;
//    @Transactional
//    public FeedLikeResponse createLike(CreateLikeRequest req, User me) {
//        // 1) Feed가 실제로 존재하는지 확인
//        Feed feed = feedService.findById(req.getFeedId())
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Feed not found with id " + req.getFeedId()));
//
//        Likes likes = new Likes(feed, me);
//        likes.liked();
//        likeRepository.save(likes);
//
//        return feedService.increaseLike(feed);
//
//    }
//
//    @Transactional
//    public FeedLikeResponse deleteLike(Long id, User me) {
//        Feed feed = feedService.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Feed not found with id " + id));
//
//        Likes likes = likeRepository.findByFeedAndUser(feed, me);
//        likeRepository.delete(likes);
//
//        return feedService.decreaseLike(feed);
//    }

    @Transactional
    public FeedLikeResponse createLike(CreateLikeRequest req, User me) {
        Feed feed = feedRepository.findByIdAndDeletedAtIsNull(req.getFeedId())
                .orElseThrow(() -> new RuntimeException("feed not found"));

        // 이미 좋아요면 멱등 응답
        if (likeRepository.existsByFeedAndUser(feed, me)) {
            return new FeedLikeResponse(feed.getId(), true, feed.getLikes());
        }

        likeRepository.save(new Likes(feed, me));

        expService.applyActivity(me, ActivityType.LIKE_CREATE);

        Long targetUserId = feed.getUserId();

        if(!targetUserId.equals(me.getId())){
            publisher.publishEvent(new CreateLikeNotificationRequest(
                    feed.getUserId(), me.getNickname(), feed.getId(), feed
            ));
        }

        return feedService.increaseLike(feed);   // ✅ 여기서 호출
    }

    @Transactional
    public FeedLikeResponse deleteLike(Long feedId, User me) {
        Feed feed = feedRepository.findByIdAndDeletedAtIsNull(feedId)
                .orElseThrow(() -> new RuntimeException("feed not found"));

        // 없으면 0건 삭제(멱등)
        likeRepository.deleteByFeedAndUser(feed, me);

        expService.applyActivity(me, ActivityType.LIKE_DELETE);

        return feedService.decreaseLike(feed);   // ✅ 여기서 호출
    }

    @Transactional
    public FeedLikeResponse toggle(Long feedId, User me) {
        Feed feed = feedRepository.findByIdAndDeletedAtIsNull(feedId)
                .orElseThrow(() -> new RuntimeException("feed not found"));

        boolean existed = likeRepository.existsByFeedAndUser(feed, me);
        boolean liked;

        if (existed) {
            long deleted = likeRepository.deleteByFeedAndUser(feed, me);
            expService.applyActivity(me, ActivityType.LIKE_DELETE);
            liked = false;
        } else {
            try {
                likeRepository.save(new Likes(feed, me));
                expService.applyActivity(me, ActivityType.LIKE_CREATE);
            } catch (DataIntegrityViolationException ignored) {
                // 동시 클릭으로 유니크 제약에 걸려도 최종 liked=true로 처리
            }
            liked = true;
        }

        // 소스 오브 트루스: 테이블에서 재계산하여 드리프트 제거
        int likeCount = (int) likeRepository.countByFeed(feed);
        feed.setLikes(likeCount);
        feedRepository.save(feed);

        return new FeedLikeResponse(feed.getId(), liked, likeCount);
    }

    @Transactional(readOnly = true)
    public boolean existsLike(Feed feed, User user) {
        return likeRepository.existsByFeedAndUser(feed, user);
    }
}
