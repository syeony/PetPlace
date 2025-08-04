package com.minjeok4go.petplace.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
@AllArgsConstructor
public class ChatRoomDTO {
    private Integer chatRoomId;
    private Integer userId1;
    private Integer userId2;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}

