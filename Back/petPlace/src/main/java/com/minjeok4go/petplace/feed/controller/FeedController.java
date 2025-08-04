package com.minjeok4go.petplace.feed.controller;

import com.minjeok4go.petplace.feed.model.FeedDetailDto;
import com.minjeok4go.petplace.feed.model.FeedDto;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.feed.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feeds")  // ✅ RESTful 규칙: 복수형 사용
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final RecommendationService recommendationService;

    // ✅ 피드 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<FeedDetailDto> getFeed(@PathVariable Long id) {
        return ResponseEntity.ok(feedService.getFeedDetail(id));
    }

    // ✅ 추천 피드 리스트 조회
    @GetMapping("/recommend")
    public ResponseEntity<List<FeedDto>> getRecommendedFeeds(
            @RequestParam(name = "user_id") Long userId, // ✅ 파라미터 이름 snake_case 적용
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(recommendationService.getRecommendedFeeds(userId, page, size));
    }
}
