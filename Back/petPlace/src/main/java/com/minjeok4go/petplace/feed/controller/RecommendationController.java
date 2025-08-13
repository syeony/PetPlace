package com.minjeok4go.petplace.feed.controller;

import com.minjeok4go.petplace.feed.dto.FeedListResponse;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import com.minjeok4go.petplace.user.service.CBFRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final CBFRecommendationService recommendationService; // 변수명 카멜케이스로

    private final UserRepository userRepository;

    // 의미상 POST가 맞음 (배치 실행이라는 상태 변경)
    @PostMapping("/batch")
    public ResponseEntity<Void> triggerBatch() {
        log.info("[/api/recommend/batch] trigger");
        recommendationService.batchRecommendationToRedisAsync(); // 비동기
        return ResponseEntity.accepted().build(); // 202
    }

//    @GetMapping("/group")
//    public ResponseEntity<List<FeedListResponse>> getRecommendedFeeds(
//            @AuthenticationPrincipal User user,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        if (user == null) {
//            // 테스트용: 실제 존재하는 유저 ID로 교체
//            user = userRepository.findById(2L)
//                    .orElseThrow(() -> new IllegalStateException("테스트 유저 없음"));
//        }
//        return ResponseEntity.ok(recommendationService.getRecommendedFeeds(user, page, size));
//    }

    @GetMapping("/group")
    public ResponseEntity<List<FeedListResponse>> getRecommendedFeeds(
            @AuthenticationPrincipal String principal,  // <= String으로 받기
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (principal == null) {
            // 인증 필수라면 401
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }
        long userId = Long.parseLong(principal); // "8" → 8
        return ResponseEntity.ok(recommendationService.getRecommendedFeeds(userId, page, size));
    }

}
