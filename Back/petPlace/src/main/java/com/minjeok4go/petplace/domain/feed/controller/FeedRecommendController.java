package com.minjeok4go.petplace.domain.feed.controller;

import com.minjeok4go.petplace.domain.feed.model.FeedDto;
import com.minjeok4go.petplace.domain.feed.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class FeedRecommendController {

    private final RecommendationService recommendationService;

    public FeedRecommendController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
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
