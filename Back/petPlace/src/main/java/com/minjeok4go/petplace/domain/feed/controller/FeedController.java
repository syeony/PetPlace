package com.minjeok4go.petplace.domain.feed.controller;

import com.minjeok4go.petplace.domain.feed.model.FeedDetailDto;
import com.minjeok4go.petplace.domain.feed.model.FeedDto;
import com.minjeok4go.petplace.domain.feed.service.FeedService;
import com.minjeok4go.petplace.domain.feed.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;
    private final RecommendationService recommendationService;

    @GetMapping("/{id}")
    public ResponseEntity<FeedDetailDto> getFeed(@PathVariable Long id) {
        return ResponseEntity.ok(feedService.getFeedDetail(id));
    }

    // 추천 피드 리스트 API
    @GetMapping("/recommend")
    public List<FeedDto> getRecommendedFeeds(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return recommendationService.getRecommendedFeeds(userId, page, size);
    }
}
