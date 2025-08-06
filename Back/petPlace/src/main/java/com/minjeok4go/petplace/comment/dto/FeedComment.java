package com.minjeok4go.petplace.comment.dto;

import com.minjeok4go.petplace.comment.entity.Comment;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeedComment extends MyComment {

    private List<FeedComment> replies;

    public static FeedComment from(Comment comment) {
        return FeedComment.builder()
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
                        .map(FeedComment::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
