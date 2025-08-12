package com.minjeok4go.petplace.feed.controller;

import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.feed.dto.CreateFeedRequest;
import com.minjeok4go.petplace.feed.dto.DeleteFeedResponse;
import com.minjeok4go.petplace.feed.dto.FeedDetailResponse;
import com.minjeok4go.petplace.feed.service.FeedService;
//import com.minjeok4go.petplace.feed.service.RecommendationService;
import com.minjeok4go.petplace.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Feed API", description = "피드 API")
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FeedController {

    private final FeedService feedService;
//    private final RecommendationService recommendationService;
    private final AuthService authService;

    @Operation(
        summary = "피드 단건 조회",
        description = "Path 변수로 넘어온 피드 ID 에 해당하는 피드를 상세 정보와 함께 반환합니다."
    )
    @GetMapping("/{id}")
    public FeedDetailResponse getFeed(@PathVariable Long id,
                                      @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return feedService.getFeedDetail(id, me);
    }

    @Operation(
            summary = "내가 작성한 피드 조회",
            description = "Path 변수로 넘어온 피드 ID 에 해당하는 피드를 상세 정보와 함께 반환합니다."
    )
    @GetMapping("/me")
    public List<FeedDetailResponse> getMyFeed(@AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return feedService.findByUserId(me);
    }

//    @Operation(
//        summary = "유저 데이터 기반 추천 피드",
//        description = "토큰으로 받아온 유저 ID에 적합한 피드들을\n" +
//                "Param 변수로 넘어온 현재 페이지와 사이즈 만큼 반환합니다."
//    )
//    @GetMapping("/recommend")
//    public ResponseEntity<List<FeedListResponse>> getRecommendedFeeds(@RequestParam(defaultValue = "0") int page,
//                                                                      @RequestParam(defaultValue = "20") int size,
//                                                                      @AuthenticationPrincipal String tokenUserId
//    ) {
//        User me = authService.getUserFromToken(tokenUserId);
//        return ResponseEntity.ok(recommendationService.getRecommendedFeeds(me, page, size));
//    }

    @Operation(
        summary = "피드 등록",
        description = "토큰으로 받아온 유저 ID와 Body의 데이터를 바탕으로 피드를 작성합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedDetailResponse createFeed(@Valid @RequestBody CreateFeedRequest req,
                                         @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return feedService.createFeed(req, me);
    }

    @Operation(
        summary = "피드 수정",
        description = "토큰으로 받아온 유저 ID와 Path 변수의 피드 ID,\n" +
                "Body의 데이터를 바탕으로 피드를 수정합니다.\n" +
                "토큰의 유저와 피드의 유저가 일치하지 않을경우 403을 반환합니다."
    )
    @PutMapping("/{id}")
    public FeedDetailResponse updateFeed(@PathVariable Long id,
                                         @Valid @RequestBody CreateFeedRequest req,
                                         @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return feedService.updateFeed(id, req, me);
    }

    @Operation(
        summary = "피드 삭제",
        description = "토큰으로 받아온 유저 ID와 Path 변수의 피드 ID로 피드를 삭제합니다.\n" +
                "토큰의 유저와 피드의 유저가 일치하지 않을경우 403을 반환합니다."
    )
    @DeleteMapping("/{id}")
    public DeleteFeedResponse deleteFeed(@PathVariable Long id,
                                         @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return feedService.deleteFeed(id, me);
    }
}
