package com.minjeok4go.petplace.chat.repository;

import com.minjeok4go.petplace.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> { //
    @Query("SELECT c FROM Chat c WHERE c.chatRoom.id = :chatRoomId ORDER BY c.createdAt ASC")
    List<Chat> findAllByChatRoomId(@Param("chatRoomId") Integer chatRoomId);

    List<Chat> findByChatRoom_IdOrderByCreatedAtAsc(Integer chatRoomId);
    int countByChatRoom_IdAndIdGreaterThanAndUser_IdNot(Integer chatRoomId, Long id, Integer userId);
}
