package com.minjeok4go.petplace.domain.feed.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private Long cid;
    private Long fid;
    private String content;
    private Long uid;
    private String userNick;
    private String userImg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<CommentDto> replies;

    public static CommentDto from(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .uid(comment.getUid())
                .userNick(comment.getUserNick())
                .userImg(comment.getUserImg())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .deletedAt(comment.getDeletedAt())
                .replies(comment.getReplies().stream()
                        .map(CommentDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

}
