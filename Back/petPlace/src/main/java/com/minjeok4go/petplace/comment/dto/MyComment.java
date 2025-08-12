package com.minjeok4go.petplace.comment.dto;

import com.minjeok4go.petplace.comment.entity.Comment;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MyComment {

    private Long id;
    private Long parentCommentId;
    private Long feedId;
    private String content;
    private Long userId;
    private String userNick;
    private String userImg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    public MyComment(Comment comment){
        this.id = comment.getId();
        this.parentCommentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        this.feedId = comment.getFeed().getId();
        this.content = comment.getContent();
        this.userId = comment.getUserId();
        this.userNick = comment.getUserNick();
        this.userImg = comment.getUserImg();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.deletedAt = comment.getDeletedAt();
    }
}
