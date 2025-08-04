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
    public void updateLastRead(Integer userId, Integer chatRoomId, Integer lastReadCid) {
        UserChatRoom ucr = ucrRepo.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalStateException("UserChatRoom not found"));
        ucr.setLastReadCid(lastReadCid);
        ucrRepo.save(ucr);
    }

    /**
     * 퇴장 처리: 사용자가 채팅방을 완전히 나간 시각을 기록합니다.
     */
    @Transactional
    public void leaveChatRoom(Integer userId, Integer chatRoomId) {
        UserChatRoom ucr = ucrRepo.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalStateException("UserChatRoom not found"));
        ucr.setLeaveAt(java.time.LocalDateTime.now());
        ucrRepo.save(ucr);
    }

    /**
     * Unread count 조회: 사용자가 아직 읽지 않은 메시지 개수를 반환합니다.
     */
    @Transactional(readOnly = true)
    public int getUnreadCount(Integer userId, Integer chatRoomId) {
        UserChatRoom ucr = ucrRepo.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalStateException("UserChatRoom not found"));
        // 마지막 읽은 메시지 ID가 없으면 0으로 간주
        long lastRead = ucr.getLastReadCid() != null ? ucr.getLastReadCid() : 0L;
        // chatRepo 에는 아래 쿼리 메서드가 필요합니다:
        // int countByChatRoomIdAndIdGreaterThan(Long chatRoomId, Long id);
        return chatRepo.countByChatRoom_IdAndIdGreaterThan(chatRoomId, lastRead);
    }
    @Transactional
    public void joinChatRoom(Integer userId, Integer chatRoomId) {
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
            ucr.setLastReadCid(0);
            ucrRepo.save(ucr);
        }
    }
}
