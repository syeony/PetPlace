package com.minjeok4go.petplace.chat.dto;

import lombok.Data;

@Data
public class ReadUpdateDTO {
    private Integer userId;
    private Integer chatRoomId;
    private Integer lastReadCid;
}
