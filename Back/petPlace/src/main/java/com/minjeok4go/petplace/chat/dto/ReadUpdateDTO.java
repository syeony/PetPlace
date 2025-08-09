package com.minjeok4go.petplace.chat.dto;

import lombok.Data;

@Data
public class ReadUpdateDTO {
    private Long userId;
    private Long chatRoomId;
    private Long lastReadCid;
}
