package com.minjeok4go.petplace.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChatRoomRequest {
    private Integer userId1;
    private Integer userId2;
}
