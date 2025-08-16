package com.minjeok4go.petplace.chat.repository;

import com.minjeok4go.petplace.chat.entity.UserChatRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    Optional<com.minjeok4go.petplace.chat.entity.UserChatRoom> findByUserIdAndChatRoomId(Long userId, Long chatRoomId); //userid와 chatRoomId주어진 값을 찾아서 반환
    List<UserChatRoom> findByUserId(Long userId);
    @EntityGraph(attributePaths = {"user", "user.region"})
    List<UserChatRoom> findByChatRoom_Id(Long chatRoomId);

}
