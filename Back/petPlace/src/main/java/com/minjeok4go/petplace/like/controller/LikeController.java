package com.minjeok4go.petplace.like.controller;

import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.feed.dto.FeedDetailResponse;
import com.minjeok4go.petplace.feed.dto.FeedLikeResponse;
import com.minjeok4go.petplace.feed.dto.FeedListResponse;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.like.dto.CreateLikeRequest;
import com.minjeok4go.petplace.like.service.LikeService;
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

@Tag(name = "Like API", description = "좋아요 API")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class LikeController {

    private final LikeService likeService;
    private final AuthService authService;
    private final FeedService feedService;

    @Operation(
            summary = "좋아요 피드 조회",
            description = "토큰으로 받아온 유저 정보에 해당하는 피드들을 반환합니다."
    )
    @GetMapping("/me")
    public List<FeedDetailResponse> getLikeFeed(@AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return feedService.findByIdWhereUserId(me);
    }

    @Operation(
            summary = "좋아요 등록",
            description = "토큰으로 받아온 유저 정보와 Body의 데이터를 바탕으로 좋아요 작업을 수행합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedLikeResponse createLike(
            @Valid @RequestBody CreateLikeRequest req,
            @AuthenticationPrincipal String tokenUserId
    ) {
        User me = authService.getUserFromToken(tokenUserId);
        return likeService.createLike(req, me);
    }

    @Operation(
            summary = "좋아요 취소",
            description = "토큰으로 받아온 유저 정보와 Body의 데이터를 바탕으로 좋아요 취소 작업을 수행합니다."
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedLikeResponse deleteLike(
            @Valid @PathVariable Long id,
            @AuthenticationPrincipal String tokenUserId
    ) {
        User me = authService.getUserFromToken(tokenUserId);
        return likeService.deleteLike(id, me);
    }
    @Operation(
            summary= "토글",
            description = "토글로 좋아요, 취소 수행 "
    )
    @PatchMapping("/toggle/{feedId}/")
    public FeedLikeResponse toggleLike(@PathVariable Long feedId,
                                       @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return likeService.toggle(feedId, me);
    }

}
