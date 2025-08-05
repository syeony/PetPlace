package com.minjeok4go.petplace.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDTO {
    private Integer chatRoomId;
    private Integer user1Id;
    private String user1Nickname;
    private Integer user2Id;
    private String user2Nickname;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount; // optional
}
