package com.minjeok4go.petplace.chat.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long chatId;
    private Long chatRoomId;
    private Long userId;
    private String nickname;
    private String message;
    private List<String> imageUrls; // 여러 이미지 URL
    private LocalDateTime createdAt;
}
