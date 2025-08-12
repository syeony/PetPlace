package com.minjeok4go.petplace.like.repository;

import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.like.entity.Likes;
import com.minjeok4go.petplace.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    boolean existsByFeedAndUser(Feed feed, User user);
    Likes findByFeedAndUser(Feed feed, User user);

    // 유저가 좋아요한 피드 ID 목록
    @Query("select l.feed.id from Likes l where l.user.id = :uid")
    List<Long> findFeedIdsByUserId(@Param("uid") Long userId);

    @Query("""
   select l.feed.id
   from Likes l
   where l.user.id = :uid
   order by l.likedAt desc
""")
    Page<Long> findRecentFeedIdsByUser(@Param("uid") Long userId, Pageable pageable);
// 사용: findRecentFeedIdsByUser(uid, PageRequest.of(0, 20))



}
