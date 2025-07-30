package com.minjeok4go.petplace.domain.feed.service;

import com.minjeok4go.petplace.domain.feed.model.Feed;
import com.minjeok4go.petplace.domain.feed.model.FeedDto;
import com.minjeok4go.petplace.domain.feed.repository.FeedDummyRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final FeedDummyRepository feedDummyRepository;

    public RecommendationService(FeedDummyRepository feedDummyRepository) {
        this.feedDummyRepository = feedDummyRepository;
    }

    public List<FeedDto> getRecommendedFeeds(Long userId, int page, int size) {
        // 1. 콜드 스타트 판단
        boolean isColdStart = checkColdStart(userId);

        // 2. 전체 피드 조회
        List<Feed> allFeeds = feedDummyRepository.findAll();

        // 3. 추천 점수 계산 및 리스트 생성
        List<FeedDto> scoredFeedDtos = allFeeds.stream()
                .map(feed -> {
                    double contentScore = calcContentScore(userId, feed);
                    double cfScore = isColdStart ? 0.0 : calcCollaborativeScore(userId, feed);

                    double hybridScore;
                    if (isColdStart) {
                        double popularScore = getPopularScore(feed);
                        // 콜드 스타트 시 콘텐츠 기반 가중치 크게, 인기 점수도 반영
                        hybridScore = 0.8 * contentScore + 0.2 * popularScore;
                    } else {
                        // 일반 사용자 하이브리드 가중치
                        hybridScore = 0.6 * contentScore + 0.4 * cfScore;
                    }

                    return FeedDto.from(feed, hybridScore);
                })
                .sorted(Comparator.comparingDouble(FeedDto::getScore).reversed())
                .collect(Collectors.toList());

        // 4. 페이지네이션
        int fromIdx = page * size;
        int toIdx = Math.min(fromIdx + size, scoredFeedDtos.size());
        if (fromIdx >= toIdx) {
            return Collections.emptyList();
        }

        return scoredFeedDtos.subList(fromIdx, toIdx);
    }

    // 콜드 스타트 판단 예시 - 실제로는 좋아요, 댓글 등 데이터로 판단
    private boolean checkColdStart(Long userId) {
        // TODO: DB에서 사용자 행동 데이터 카운트 조회
        // 예: likeCount + commentCount < 임계치(3~5) 이면 콜드 스타트
        // 임시로 아래처럼 하드코딩 가능
        int userActionCount = getUserActionCount(userId);
        return userActionCount < 3;
    }

    private int getUserActionCount(Long userId) {
        // TODO: 좋아요, 댓글, 조회수 등 행동 로그 숫자 합산 반환 (DB 조회)
        // 임시: 0 반환해서 항상 콜드 스타트로 처리
        return 0;
    }

    private double calcContentScore(Long userId, Feed feed) {
        // 예시: "사용자의 선호 태그/카테고리"를 임의로 정의 나중에는  사용자별 프로필 에서 실제 데이터를 가져와서 추천 알고리즘 개선
        //  TF-IDF(중요도 가중치), 코사인 유사도, 워드 임베딩를 통해 개선
        List<Integer> userFavTags = List.of(1, 3, 7); // ex: dog, 산책, 미용
        Set<Integer> feedTags = new HashSet<>(feed.getTags());

        // 태그 겹침 개수 세기
        long matchCount = userFavTags.stream().filter(feedTags::contains).count();

        // 카테고리(문자열) 매칭
        String userFavCategory = "0";
        boolean categoryMatch = userFavCategory.equals(feed.getCategory());

        // 가중치 조합 (예시)
        double tagScore = matchCount / (double) userFavTags.size(); // 0~1
        double categoryScore = categoryMatch ? 1.0 : 0.0;

        // 합산 (70% 태그 + 30% 카테고리)
        return 0.7 * tagScore + 0.3 * categoryScore;
    }

    private double calcCollaborativeScore(Long userId, Feed feed) {
        // 임시: "userId가 과거에 좋아요한 feed들의 태그 리스트"를 임의로 가정
        // (실제론 DB에서 행동로그를 불러와서 유사도 계산) 해당 유저가 좋아요한 태그를 조회해서 동적 호출
        // 추후에 댓글이나 태그 빈도수, TF-IDF를 사용해 개선
        List<Integer> likedTags = List.of(2, 3, 6); // ex: cat, 산책, 입양
        Set<Integer> feedTags = new HashSet<>(feed.getTags());

        // 유사도: 내가 좋아요한 태그와 피드의 태그 겹침 개수 비율
        long matchCount = likedTags.stream().filter(feedTags::contains).count();
        double tagScore = matchCount / (double) likedTags.size(); // 0~1

        // (예시) 내가 좋아요한 태그 1개라도 포함돼 있으면 1.0점, 아니면 0점
        // double tagScore = matchCount > 0 ? 1.0 : 0.0;

        // 또는 내가 좋아요한 피드 작성자와 같은 작성자면 보너스
        // Long myFavoriteWriter = 15L;
        // double writerScore = myFavoriteWriter.equals(feed.getUid()) ? 1.0 : 0.0;

        // 가중치(100% 태그 기반, 확장시 여러 특성 가중합 가능)
        return tagScore;
    }

    private double getPopularScore(Feed feed) {
        // 예: 인기 기준 최대값을 임의로 설정 (프로젝트/더미 데이터에 맞게 조정)
        // 나중에 실제 db를 구현하면 상위 1%를 구하는 쿼리로 기준 값을 불러온 뒤 정규화해서 로직 개선
        int maxView = 500;    // 더미 데이터 최대 조회수
        int maxLike = 30;     // 더미 데이터 최대 좋아요 수

        // 조회수, 좋아요 수를 0~1 사이 점수로 정규화
        double viewScore = (double) feed.getView() / maxView;
        double likeScore = (double) feed.getLike() / maxLike;

        // 가중치 조합(예: 좋아요 70%, 조회수 30%)
        return 0.7 * likeScore + 0.3 * viewScore;
    }

}
