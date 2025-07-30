package com.minjeok4go.petplace.domain.feed.model;

import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feed {
    private Long id;
    private String content;
    private Long uid;             // 작성자 유저 인덱스
    @JsonProperty("user_nick")
    private String userNick;      // 작성자 닉네임 (ERD 기준 user_nick)
    @JsonProperty("user_img")
    private String userImg;       // 작성자 프로필 이미지
    private Integer rid;          // 지역 인덱스
    private String category;      // ENUM 값 ("0", "1", "2", ...)
    private List<Integer> tags;   // 태그 id 리스트
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    private Integer like;
    private Integer view;
}
