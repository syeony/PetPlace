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
    public static FeedComment from(Comment comment) {
        if (comment == null) return null;

        // 자식 댓글 컬렉션 이름에 맞게 하나만 쓰세요: getReplies() 또는 getChildren()
        // 아래는 getReplies() 기준 예시
        List<FeedComment> replies =
                comment.getReplies() == null ? List.of()
                        : comment.getReplies().stream()
                        .map(FeedComment::from)
                        .toList();

        return new FeedComment(comment, replies); // super(comment) 호출은 생성자에서 처리됨
    }

}
