package com.minjeok4go.petplace.chat.service;

import com.minjeok4go.petplace.chat.dto.ChatMessageDTO;
import com.minjeok4go.petplace.chat.entity.Chat;
import com.minjeok4go.petplace.chat.entity.ChatRoom;
import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.chat.repository.ChatRepository;
import com.minjeok4go.petplace.chat.repository.ChatRoomRepository;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UserChatRoomService userChatRoomService;

    // 채팅 저장 및 DTO 반환 (chatId 포함)
    public ChatMessageDTO saveAndReturnMessage(ChatMessageDTO dto) {
        try {
            Chat chat = new Chat();
            chat.setChatRoom(chatRoomRepository.findById(dto.getChatRoomId()).orElseThrow());
            chat.setUser(userRepository.findById(dto.getUserId()).orElseThrow());
            chat.setMessage(dto.getMessage());
            chat.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());

            Chat saved = chatRepository.save(chat);
            // 내 메세지는 바로읽음 처리
            userChatRoomService.updateLastRead(dto.getUserId(), dto.getChatRoomId(), saved.getId().longValue());

            System.out.println(">>> 저장된 Chat ID: " + saved.getId());

            ChatRoom chatRoom = saved.getChatRoom();
            chatRoom.setLastMessage(saved.getMessage());
            chatRoom.setLastMessageAt(saved.getCreatedAt());
            chatRoomRepository.save(chatRoom);

            // 이미지 저장
            List<String> imageUrls = dto.getImageUrls() != null ? dto.getImageUrls() : List.of();
            if (!imageUrls.isEmpty()) {
                List<Image> images = imageUrls.stream()
                        .map(url -> new Image(saved.getId().longValue(), ImageType.CHAT, url, 0))
                        .toList();
                imageRepository.saveAll(images);
            }

            // 유저 닉네임 불러오기
            String nickname = saved.getUser().getNickname();

            // chatId(PK) 포함해서 DTO로 반환
            return new ChatMessageDTO(
                    saved.getId().longValue(),
                    saved.getChatRoom().getId(),
                    saved.getUser().getId(),
                    nickname,
                    saved.getMessage(),
                    imageUrls,
                    saved.getCreatedAt()
            );
        } catch (Exception e) {
            // 여기서 에러 내용을 로그로 뽑아줌!
            System.out.println(">>> 채팅 저장 중 예외 발생!!");
            e.printStackTrace();
            throw e; // 예외 재전파(서버에서 에러 응답됨)
        }
    }

    public List<ChatMessageDTO> getMessagesByRoom(Long chatRoomId) {
//        List<Chat> chats = chatRepository.findByChatRoom_IdOrderByCreatedAtAsc(chatRoomId);
        List<Chat> chats = chatRepository.findAllByChatRoomId(chatRoomId); // 여기!

        System.out.println("조회된 채팅 개수: " + chats.size()); // 추가!

        return chats.stream().map(chat -> {
            List<String> imageUrls = chat.getImages() != null
                    ? chat.getImages().stream().map(Image::getSrc).toList()
                    : List.of();
            return new ChatMessageDTO(
                    chat.getId().longValue(),
                    chat.getChatRoom().getId(),
                    chat.getUser().getId(),
                    chat.getUser().getNickname(),
                    chat.getMessage(),
                    imageUrls,
                    chat.getCreatedAt()
            );
        }).toList();
    }
}

