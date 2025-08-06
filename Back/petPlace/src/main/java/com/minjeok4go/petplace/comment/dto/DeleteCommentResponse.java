// DeleteCommentResponse.java
package com.minjeok4go.petplace.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteCommentResponse {

    /** 삭제된 댓글의 ID */
    private Long id;
}
