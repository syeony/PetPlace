package com.minjeok4go.petplace.feed.controller;

import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.feed.dto.CreateFeedRequest;
import com.minjeok4go.petplace.feed.dto.DeleteFeedResponse;
import com.minjeok4go.petplace.feed.dto.FeedDetailResponse;
import com.minjeok4go.petplace.feed.dto.FeedListResponse;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.feed.service.RecommendationService;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "피드")
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FeedController {

    private final FeedService feedService;
    private final RecommendationService recommendationService;
    private final UserService userService;

    @Operation(summary = "피드 단건 조회")
    @GetMapping("/{id}")
    public FeedDetailResponse getFeed(@PathVariable Long id) {
        return feedService.getFeedDetail(id);
    }

    @Operation(summary = "유저 데이터 기반 추천 피드")
    @GetMapping("/recommend")
    public ResponseEntity<List<FeedListResponse>> getRecommendedFeeds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal TokenDto.UserInfo tokenUser
    ) {
        User me = userService.getUserFromToken(tokenUser);
        return ResponseEntity.ok(recommendationService.getRecommendedFeeds(me.getId().longValue(), page, size));
    }

    @Operation(summary = "피드 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedDetailResponse createFeed(@Valid @RequestBody CreateFeedRequest req,
                                         @AuthenticationPrincipal TokenDto.UserInfo tokenUser) {
        User me = userService.getUserFromToken(tokenUser);
        return feedService.createFeed(req, me);
    }


    @Operation(summary = "피드 수정")
    @PutMapping("/{id}")
    public FeedDetailResponse updateFeed(@PathVariable Long id,
                                         @Valid @RequestBody CreateFeedRequest req,
                                         @AuthenticationPrincipal TokenDto.UserInfo tokenUser) {
        User me = userService.getUserFromToken(tokenUser);
        return feedService.updateFeed(id, req, me);
    }


    @Operation(summary = "피드 삭제")
    @DeleteMapping("/{id}")
    public DeleteFeedResponse deleteFeed(@PathVariable Long id,
                                         @AuthenticationPrincipal TokenDto.UserInfo tokenUser) {
        User me = userService.getUserFromToken(tokenUser);
        return feedService.deleteFeed(id, me);
    }
}
