// CreateCommentRequest.java
package com.minjeok4go.petplace.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {

    @NotNull
    private Long feedId;

    private Long parentCommentId;

    @NotBlank
    private String content;
}
