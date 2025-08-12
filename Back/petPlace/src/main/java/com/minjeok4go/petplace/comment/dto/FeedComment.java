package com.minjeok4go.petplace.comment.dto;

import com.minjeok4go.petplace.comment.entity.Comment;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeedComment extends MyComment {

    private List<FeedComment> replies;

    public FeedComment(Comment comment, List<FeedComment> replies) {
        super(comment);
        this.replies = replies;
    }
}
