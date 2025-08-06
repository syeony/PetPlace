package com.minjeok4go.petplace.chat.controller;

import com.minjeok4go.petplace.chat.dto.ChatMessageDTO;
import com.minjeok4go.petplace.chat.dto.ChatRoomDTO;
import com.minjeok4go.petplace.chat.dto.ChatRoomParticipantDTO;
import com.minjeok4go.petplace.chat.dto.CreateChatRoomRequest;
import com.minjeok4go.petplace.chat.service.ChatRoomService;
import com.minjeok4go.petplace.chat.service.ChatService;
import com.minjeok4go.petplace.chat.service.UserChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserChatRoomService userChatRoomService;
    private final ChatService chatService;

    public ChatRoomController(
            ChatRoomService chatRoomService,
            UserChatRoomService userChatRoomService,
            ChatService chatService
    ) {
        this.chatRoomService = chatRoomService;
        this.userChatRoomService = userChatRoomService;
        this.chatService = chatService; // 이 줄 추가!
    }
    // 채팅방 생성
    @Operation(summary = "채팅방 생성")
    @PostMapping("/room")
    public ResponseEntity<?> createChatRoom(@RequestBody CreateChatRoomRequest dto) {
        ChatRoomDTO chatRoom = chatRoomService.createChatRoom(dto.getUserId1(), dto.getUserId2());

        //  두 사용자에 대해 joinChatRoom 호출 -> 입장시 joinchatroom을 통해 채팅방이 없으면 새로 생성하고  있으면 채팅방에 참여중이라고 저장
        userChatRoomService.joinChatRoom(dto.getUserId1(), chatRoom.getChatRoomId());
        userChatRoomService.joinChatRoom(dto.getUserId2(), chatRoom.getChatRoomId());

        return ResponseEntity.ok(chatRoom);
    }
    // 채팅방 목록 조회
    @Operation(summary = "채팅방 목록 조회")
    @GetMapping("/rooms")
    public ResponseEntity<?> getChatRooms(@RequestParam Long userId) {
        List<ChatRoomDTO> rooms = chatRoomService.getChatRoomsByUser(userId);
        return ResponseEntity.ok(rooms);
    }

    // ⬇️ 추가: 안 읽은 메시지 수 조회
    @Operation(summary = "채팅방 안 읽은 메시지 수 조회")
    @GetMapping("/rooms/{chatRoomId}/unread")
    public ResponseEntity<Integer> getUnreadCount(
            @PathVariable Long chatRoomId,
            @RequestParam Long userId
    ) {
        int count = userChatRoomService.getUnreadCount(userId, chatRoomId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/rooms/{chatRoomId}/join")
    public ResponseEntity<?> joinChatRoom(
            @PathVariable Long chatRoomId,
            @RequestParam Long userId
    ) {
        userChatRoomService.joinChatRoom(userId, chatRoomId); // ⬅️ 이거 꼭 필요!
        return ResponseEntity.ok().build();
    }
    // 방 기준으로 참여자 정보 가져오기
    @GetMapping("/rooms/{chatRoomId}/participants")
    public List<ChatRoomParticipantDTO> getParticipants(@PathVariable Long chatRoomId) {
        return chatRoomService.getParticipantDTOs(chatRoomId);
    }



    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable Long chatRoomId) {
        List<ChatMessageDTO> messages = chatService.getMessagesByRoom(chatRoomId);
        return ResponseEntity.ok(messages);
    }

}

