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
    private Long parentCommentId;   // ✅ cid → parent_comment_id
    private Long feedId;            // ✅ fid → feed_id
    private String content;
    private Long userId;            // ✅ uid → user_id
    private String userNick;
    private String userImg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<CommentDto> replies;

    public static CommentDto from(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .feedId(comment.getFeed().getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
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
