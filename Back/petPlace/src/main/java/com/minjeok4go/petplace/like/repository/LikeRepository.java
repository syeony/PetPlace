package com.minjeok4go.petplace.like.repository;

import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.like.entity.Likes;
import com.minjeok4go.petplace.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    boolean existsByFeedAndUser(Feed feed, User user);
    Likes findByFeedAndUser(Feed feed, User user);
}
