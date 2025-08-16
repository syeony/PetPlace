package com.minjeok4go.petplace.chat.service;

import com.minjeok4go.petplace.chat.dto.ChatRoomDTO;
import com.minjeok4go.petplace.chat.entity.ChatRoom;
import com.minjeok4go.petplace.chat.repository.ChatRoomRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.minjeok4go.petplace.chat.entity.UserChatRoom;
import com.minjeok4go.petplace.chat.dto.ChatRoomParticipantDTO;
import com.minjeok4go.petplace.chat.repository.UserChatRoomRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository ucrRepo;
    private final UserChatRoomService userChatRoomService;


    public ChatRoomService(ChatRoomRepository chatRoomRepository, UserRepository userRepository, UserChatRoomRepository ucrRepo, UserChatRoomService userChatRoomService) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.ucrRepo = ucrRepo;
        this.userChatRoomService = userChatRoomService;
    }

    public ChatRoomDTO createChatRoom(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId2).orElseThrow();

        // 중복 방지 (이미 있는지 체크)
        Optional<ChatRoom> existingRoom =
                chatRoomRepository.findByUser1AndUser2(user1, user2)
                        .or(() -> chatRoomRepository.findByUser2AndUser1(user1, user2));
        if (existingRoom.isPresent()) {
            ChatRoom cr = existingRoom.get();
            // **이미 방이 있으면, joinChatRoom도 해주면 좋음**
            userChatRoomService.joinChatRoom(userId1, cr.getId());
            userChatRoomService.joinChatRoom(userId2, cr.getId());
            return new ChatRoomDTO(cr.getId(), userId1, userId2, cr.getLastMessage(), cr.getLastMessageAt());
        }

        // 새로 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUser1(user1);
        chatRoom.setUser2(user2);
        chatRoom.setLastMessage("");
        ChatRoom saved = chatRoomRepository.save(chatRoom);

        // **방 생성과 동시에 두 유저 join 처리**
        userChatRoomService.joinChatRoom(userId1, saved.getId());
        userChatRoomService.joinChatRoom(userId2, saved.getId());

        return new ChatRoomDTO(saved.getId(), userId1, userId2, "", null);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomParticipantDTO> getParticipantDTOs(Long chatRoomId) {
        return ucrRepo.findByChatRoom_Id(chatRoomId).stream()
                .map(ucr -> {
                    User user = ucr.getUser();
                    String regionName = (user.getRegion() != null)
                            ? user.getRegion().getName()   // 필드명에 맞게 수정
                            : null;

                    return new ChatRoomParticipantDTO(
                            user.getId(),
                            user.getNickname(),
                            user.getUserImgSrc(),
                            regionName
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getChatRoomsByUser(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByUserId(userId);
        // ChatRoom → ChatRoomDto 변환
        return rooms.stream()
                .map(r -> new ChatRoomDTO(r.getId(),
                        r.getUser1().getId(),
                        r.getUser2().getId(),
                        r.getLastMessage(),
                        r.getLastMessageAt()))
                .collect(Collectors.toList());
    }
}