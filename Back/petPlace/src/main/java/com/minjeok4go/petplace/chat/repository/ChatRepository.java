package com.minjeok4go.petplace.chat.repository;

import com.minjeok4go.petplace.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> { //
    @Query("SELECT c FROM Chat c WHERE c.chatRoom.id = :chatRoomId ORDER BY c.createdAt ASC")
    List<Chat> findAllByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    List<Chat> findByChatRoom_IdOrderByCreatedAtAsc(Long chatRoomId);
    int countByChatRoom_IdAndIdGreaterThanAndUser_IdNot(Long chatRoomId, Long id, Long userId);
}
