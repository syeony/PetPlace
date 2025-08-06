package com.minjeok4go.petplace.comment.controller;

import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.comment.dto.CreateCommentRequest;
import com.minjeok4go.petplace.comment.dto.FeedComment;
import com.minjeok4go.petplace.comment.dto.DeleteCommentResponse;
import com.minjeok4go.petplace.comment.dto.MyComment;
import com.minjeok4go.petplace.comment.service.CommentService;
import com.minjeok4go.petplace.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment API", description = "댓글 API")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    @Operation(
            summary = "댓글 단건 조회",
            description = "path 변수로 넘어온 댓글 ID에 해당하는 댓글을 반환합니다."
    )
    @GetMapping("/{id}")
    public MyComment getComment(@PathVariable Long id) {
        return commentService.getCommentDetail(id);
    }

    @Operation(
            summary = "피드별 댓글 목록 조회",
            description = "feed_id 파라미터로 넘어온 피드 ID에 속한 댓글들을 반환합니다."
    )
    @GetMapping("/feed_id")
    public List<FeedComment> getCommentsByFeed(
            @RequestParam("feed_id") Long feedId
    ) {
        return commentService.getCommentsByFeed(feedId);
    }

    @Operation(
            summary = "내 댓글 목록 조회",
            description = "토큰으로 받아온 유저 정보로, 내가 작성한 댓글들을 반환합니다."
    )
    @GetMapping
    public List<MyComment> getMyComments(
            @AuthenticationPrincipal String tokenUserId
    ) {
        User me = authService.getUserFromToken(tokenUserId);
        return commentService.getCommentsByUser(me.getId());
    }

    @Operation(
            summary = "댓글 등록",
            description = "토큰으로 받아온 유저 정보와 Body의 데이터를 바탕으로 댓글을 작성합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MyComment createComment(
            @Valid @RequestBody CreateCommentRequest req,
            @AuthenticationPrincipal String tokenUserId
    ) {
        User me = authService.getUserFromToken(tokenUserId);
        return commentService.createComment(req, me);
    }

    @Operation(
            summary = "댓글 삭제",
            description = "path 변수의 댓글 ID와 토큰으로 받아온 유저 정보로 댓글을 삭제합니다.\n" +
                    "본인 댓글이 아닐 경우 403 에러를 반환합니다."
    )
    @DeleteMapping("/{id}")
    public DeleteCommentResponse deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal String tokenUserId
    ) {
        User me = authService.getUserFromToken(tokenUserId);
        return commentService.deleteComment(id, me);
    }
}
