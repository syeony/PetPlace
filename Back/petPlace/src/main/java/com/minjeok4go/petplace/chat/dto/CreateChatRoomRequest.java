package com.minjeok4go.petplace.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChatRoomRequest {
    private Long userId1;
    private Long userId2;
}
