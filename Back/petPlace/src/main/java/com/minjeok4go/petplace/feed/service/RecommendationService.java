package com.minjeok4go.petplace.feed.service;

import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.dto.FeedDto;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final FeedRepository feedRepository;

    public List<FeedDto> getRecommendedFeeds(Long userId, int page, int size) {
        boolean isColdStart = checkColdStart(userId);

        List<Feed> allFeeds = feedRepository.findAll();

        List<FeedDto> scoredFeedDtos = allFeeds.stream()
                .map(feed -> {
                    double contentScore = calcContentScore(userId, feed);
                    double cfScore = isColdStart ? 0.0 : calcCollaborativeScore(userId, feed);

                    double hybridScore;
                    if (isColdStart) {
                        double popularScore = getPopularScore(feed);
                        hybridScore = 0.8 * contentScore + 0.2 * popularScore;
                    } else {
                        hybridScore = 0.6 * contentScore + 0.4 * cfScore;
                    }

                    return FeedDto.from(feed, hybridScore);
                })
                .sorted(Comparator.comparingDouble(FeedDto::getScore).reversed())
                .collect(Collectors.toList());

        // ✅ 페이지네이션
        int fromIdx = page * size;
        int toIdx = Math.min(fromIdx + size, scoredFeedDtos.size());
        if (fromIdx >= toIdx) {
            return Collections.emptyList();
        }

        return scoredFeedDtos.subList(fromIdx, toIdx);
    }

    private boolean checkColdStart(Long userId) {
        int userActionCount = getUserActionCount(userId);
        return userActionCount < 3;
    }

    private int getUserActionCount(Long userId) {
        // TODO: DB 기반 좋아요/댓글 카운트 조회
        return 0; // 임시로 콜드 스타트 항상 true
    }

    private double calcContentScore(Long userId, Feed feed) {
        List<Long> userFavTags = List.of(1L, 3L, 7L);
        Set<Long> feedTags = feed.getFeedTags()
                .stream()
                .map(ft -> ft.getTag().getId())
                .collect(Collectors.toSet());

        long matchCount = userFavTags.stream().filter(feedTags::contains).count();

        String userFavCategory = "MYPET"; // 임시 사용자 선호 카테고리
        boolean categoryMatch = userFavCategory.equals(feed.getCategory().name());

        double tagScore = matchCount / (double) userFavTags.size();
        double categoryScore = categoryMatch ? 1.0 : 0.0;

        return 0.7 * tagScore + 0.3 * categoryScore;
    }

    private double calcCollaborativeScore(Long userId, Feed feed) {
        List<Long> likedTags = List.of(2L, 3L, 6L);
        Set<Long> feedTags = feed.getFeedTags()
                .stream()
                .map(ft -> ft.getTag().getId())
                .collect(Collectors.toSet());

        long matchCount = likedTags.stream().filter(feedTags::contains).count();
        return matchCount / (double) likedTags.size();
    }

    private double getPopularScore(Feed feed) {
        int maxView = 500;
        int maxLike = 30;

        double viewScore = (double) feed.getViews() / maxView;   // ✅ getView() → getViews()
        double likeScore = (double) feed.getLikes() / maxLike;   // ✅ getLike() → getLikes()

        return 0.7 * likeScore + 0.3 * viewScore;
    }
}
