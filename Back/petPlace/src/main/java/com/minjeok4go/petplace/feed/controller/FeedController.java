package com.minjeok4go.petplace.feed.controller;

import com.minjeok4go.petplace.feed.dto.CreateFeedRequest;
import com.minjeok4go.petplace.feed.dto.DeleteFeedResponse;
import com.minjeok4go.petplace.feed.dto.FeedDetailResponse;
import com.minjeok4go.petplace.feed.dto.FeedListResponse;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.feed.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "피드")
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final RecommendationService recommendationService;

    @Operation(summary = "피드 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<FeedDetailResponse> getFeed(@PathVariable Long id) {
        return ResponseEntity.ok(feedService.getFeedDetail(id));
    }

    @Operation(summary = "유저 데이터 기반 추천 피드")
    @GetMapping("/recommend")
    public ResponseEntity<List<FeedListResponse>> getRecommendedFeeds(
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(recommendationService.getRecommendedFeeds(userId, page, size));
    }

    @Operation(summary = "피드 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedDetailResponse createFeed(@Valid @RequestBody CreateFeedRequest req) {
        FeedDetailResponse created = feedService.createFeed(req);
        return created;
    }


    @Operation(summary = "피드 수정")
    @PutMapping("/{id}")
    public FeedDetailResponse updateFeed(@PathVariable Long id, @Valid @RequestBody CreateFeedRequest req) {
        FeedDetailResponse updated = feedService.updateFeed(id, req);
        return updated;
    }


    @Operation(summary = "피드 삭제")
    @DeleteMapping("/{id}")
    public DeleteFeedResponse deleteFeed(@PathVariable Long id) {
        DeleteFeedResponse deleted = feedService.deleteFeed(id);
        return deleted;
    }
}
