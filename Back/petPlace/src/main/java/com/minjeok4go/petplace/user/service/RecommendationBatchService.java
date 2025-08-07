package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.pet.entity.Animal;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.pet.repository.PetRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationBatchService {
    private final FeedRepository feedRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserGroupService userGroupService;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    // 1. 추천 피드 배치 작업 메서드
    // 모든 그룹별로 추천 피드 선정 및 Redis 저장
    public void batchRecommendationToRedis() {
        // 1) 모든 사용자 조회
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // 2) 각 유저의 펫 정보 조회
            List<Pet> pets = petRepository.findByUserId(user.getId());

            // 3) 유저 그룹키 생성
            String groupKey = userGroupService.determineGroupKey(user, pets);

            // 4) 이 그룹에 맞는 피드 선정 (여기선 일단 전체 인기 피드 예시)
            List<Feed> feeds = feedRepository.findTop200ByOrderByLikeCountDesc(); // 최신순·인기순 등 조합 가능

            // 5) 피드별 추천 점수 계산 & Redis 저장
            for (Feed feed : feeds) {
                double score = calculateScore(user, pets, feed); // 점수 산정 함수
                // Redis: group:{groupKey} => (feedId, score)
                redisTemplate.opsForZSet().add("group:" + groupKey, String.valueOf(feed.getId()), score); // groupkey를 redis에 저장
            }
        }
    }

    // 피드별 추천 점수 계산 로직
    private double calculateScore(User user, List<Pet> pets, Feed feed) {
        double score = 0;
        score += feed.getLikeCount() * 2; // 좋아요 개수 *2
//        score += feed.getCommentCount();  // 댓글수
//        if (feed.isDogRelated() && pets.stream().anyMatch(pet -> pet.getAnimal() == Animal.DOG)) {
//            score += 20; // 유저가 강아지 키우면 강아지 피드 가산점
//        }
        if (feed.getCreatedAt().isAfter(LocalDate.now().minusDays(2).atStartOfDay())) {
            score += 10;
        }
        // 기타 룰 추가 가능
        return score;
    }
}
