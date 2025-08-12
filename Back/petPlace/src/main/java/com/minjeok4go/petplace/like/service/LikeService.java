package com.minjeok4go.petplace.like.service;

import com.minjeok4go.petplace.feed.dto.FeedLikeResponse;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.like.dto.CreateLikeRequest;
import com.minjeok4go.petplace.like.entity.Likes;
import com.minjeok4go.petplace.like.repository.LikeRepository;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final FeedRepository feedRepository;
    private final LikeRepository likeRepository;
    private final FeedService feedService;
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
    return feedService.increaseLike(feed);   // ✅ 여기서 호출
}

    @Transactional
    public FeedLikeResponse deleteLike(Long feedId, User me) {
        Feed feed = feedRepository.findByIdAndDeletedAtIsNull(feedId)
                .orElseThrow(() -> new RuntimeException("feed not found"));

        // 없으면 0건 삭제(멱등)
        likeRepository.deleteByFeedAndUser(feed, me);
        // 이미 0일 수 있으니 음수 방지 로직이 있으면 추가
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
            liked = false;
        } else {
            try {
                likeRepository.save(new Likes(feed, me));
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
