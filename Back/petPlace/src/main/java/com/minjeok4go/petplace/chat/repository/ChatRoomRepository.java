package com.minjeok4go.petplace.chat.repository;


import com.minjeok4go.petplace.chat.entity.ChatRoom;
import com.minjeok4go.petplace.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // user1, user2 조합으로 중복 체크
//    Optional<com.minjeok4go.petplace.domain.chat.model.ChatRoom> findByUser1AndUser2(User user1, User user2);
    // 두 유저가 참여하는 채팅방이 이미 있는지 확인 (양방향 모두 체크)
    Optional<com.minjeok4go.petplace.chat.entity.ChatRoom> findByUser1AndUser2(User user1, User user2);
    Optional<com.minjeok4go.petplace.chat.entity.ChatRoom> findByUser2AndUser1(User user1, User user2);

    // 또는 순서 상관 없이
    @Query("SELECT c FROM ChatRoom c WHERE (c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)")
    Optional<com.minjeok4go.petplace.chat.entity.ChatRoom> findRoomBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    // 해당 user가 속한 방 모두 찾기
    @Query("SELECT r FROM ChatRoom r WHERE r.user1.id = :userId OR r.user2.id = :userId")
    List<com.minjeok4go.petplace.chat.entity.ChatRoom> findByUserId(@Param("userId") Long userId);
}

