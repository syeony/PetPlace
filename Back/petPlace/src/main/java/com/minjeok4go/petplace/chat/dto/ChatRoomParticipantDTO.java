package com.minjeok4go.petplace.chat.dto;

public record ChatRoomParticipantDTO(Long id, String nickname, String profileImg, String regionName
) {}
//record => getter, equals, hashCode, toString 전부 자동으로 만들어줌