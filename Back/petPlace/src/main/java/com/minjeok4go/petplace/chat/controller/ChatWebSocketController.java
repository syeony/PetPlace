package com.minjeok4go.petplace.chat.controller;

import com.minjeok4go.petplace.chat.dto.ChatMessageDTO;
import com.minjeok4go.petplace.chat.dto.ReadUpdateDTO;
import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.chat.service.ChatService;
import com.minjeok4go.petplace.chat.service.UserChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;
    private final ImageRepository imageRepository;
    private final UserChatRoomService userChatRoomService;

    // 1. 채팅 메시지 수신 & 저장 & 브로드캐스트
    @MessageMapping("/chat.sendMessage")
    public void saveMessage(@Payload ChatMessageDTO dto) {
        System.out.println(">>> 채팅 메시지 수신: " + dto);

        // 1. DB 저장 및 chatId 포함 DTO 반환
        ChatMessageDTO resultDto = chatService.saveAndReturnMessage(dto);

        // 2. 이미지 저장 (이미지 URL이 있으면)
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            List<String> imageUrls = dto.getImageUrls();
            List<Image> images = imageUrls.stream()
                    .map(url -> new Image(resultDto.getChatId(), RefType.CHAT, url, 0)) // 수정: saved → resultDto.getId()
                    .toList();
            imageRepository.saveAll(images);
        }



        // 3. 실시간 메시지 브로드캐스트 (chatId 포함)
        simpMessagingTemplate.convertAndSend(
                "/topic/chat.room." + resultDto.getChatRoomId(),
                resultDto
        );
    }

    // 2. 읽음 처리 (Mark Read)
    @MessageMapping("/chat.updateRead")
    public void updateRead(@Payload ReadUpdateDTO dto) {
        System.out.println(">>> 읽음 처리: " + dto);
        userChatRoomService.updateLastRead(dto.getUserId(), dto.getChatRoomId(), dto.getLastReadCid());
        // 읽음 처리 했다는 것을 상대방에게 알릴 때 사용 (나중에 프런트에서 채팅방을 들어올 때 읽음 처리 요청하면 자동 읽음처리)
        simpMessagingTemplate.convertAndSend("/topic/chat.room." + dto.getChatRoomId() + ".read", dto); // 누군가 읽음 처리 하면 같은 방 다른 사용자들에 전파 가능

    }
}
