package com.minjeok4go.petplace.chat.service;

import com.minjeok4go.petplace.chat.entity.ChatRoom;
import com.minjeok4go.petplace.chat.entity.UserChatRoom;
import com.minjeok4go.petplace.chat.repository.ChatRepository;
import com.minjeok4go.petplace.chat.repository.ChatRoomRepository;
import com.minjeok4go.petplace.chat.repository.UserChatRoomRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserChatRoomService {
    private final UserChatRoomRepository ucrRepo;
    private final ChatRepository chatRepo;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 읽음 처리: 사용자가 마지막으로 읽은 메시지 ID를 갱신합니다.
     */
    @Transactional
    public void updateLastRead(Long userId, Long chatRoomId, Long lastReadCid) {
        UserChatRoom ucr = ucrRepo.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalStateException("UserChatRoom not found"));
        ucr.setLastReadCid(lastReadCid);
        ucrRepo.save(ucr);
    }

    /**
     * 퇴장 처리: 사용자가 채팅방을 완전히 나간 시각을 기록합니다.
     */
    @Transactional
    public void leaveChatRoom(Long userId, Long chatRoomId) {
        UserChatRoom ucr = ucrRepo.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalStateException("UserChatRoom not found"));
        ucr.setLeaveAt(java.time.LocalDateTime.now());
        ucrRepo.save(ucr);
    }

    /**
     * Unread count 조회: 사용자가 아직 읽지 않은 메시지 개수를 반환합니다.
     */
    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId, Long chatRoomId) {
        UserChatRoom ucr = ucrRepo.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalStateException("UserChatRoom not found"));
        long lastRead = ucr.getLastReadCid() != null ? ucr.getLastReadCid() : 0L;
        // 내가 보낸 메시지는 카운트하지 않는다!
        return chatRepo.countByChatRoom_IdAndIdGreaterThanAndUser_IdNot(chatRoomId, lastRead, userId);
    }

    @Transactional
    public void joinChatRoom(Long userId, Long chatRoomId) {
        if (ucrRepo.findByUserIdAndChatRoomId(userId, chatRoomId).isEmpty()) {
            // 1. user, chatRoom 찾아서 변수에 저장
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new IllegalStateException("ChatRoom not found"));

            // 2. 로그로 확인
            System.out.println("User: " + user);
            System.out.println("ChatRoom: " + chatRoom);

            // 3. UserChatRoom에 할당
            UserChatRoom ucr = new UserChatRoom();
            ucr.setUser(user);
            ucr.setChatRoom(chatRoom);
            ucr.setLastReadCid(0L);
            ucrRepo.save(ucr);
        }
    }
}
