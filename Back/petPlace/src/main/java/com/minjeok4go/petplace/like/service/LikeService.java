package com.minjeok4go.petplace.like.service;

import com.minjeok4go.petplace.comment.dto.MyComment;
import com.minjeok4go.petplace.feed.dto.FeedLikeResponse;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.like.dto.CreateLikeRequest;
import com.minjeok4go.petplace.like.entity.Likes;
import com.minjeok4go.petplace.like.repository.LikeRepository;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final FeedService feedService;
    @Transactional
    public FeedLikeResponse createLike(CreateLikeRequest req, User me) {
        // 1) Feed가 실제로 존재하는지 확인
        Feed feed = feedService.findById(req.getFeedId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Feed not found with id " + req.getFeedId()));

        Likes likes = new Likes(feed, me);
        likes.liked();
        likeRepository.save(likes);

        return feedService.increaseLike(feed);

    }

    @Transactional
    public FeedLikeResponse deleteLike(Long id, User me) {
        Feed feed = feedService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Feed not found with id " + id));

        Likes likes = likeRepository.findByFeedAndUser(feed, me);
        likeRepository.delete(likes);

        return feedService.decreaseLike(feed);
    }
}
