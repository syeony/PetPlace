package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.comment.repository.CommentRepository;
import com.minjeok4go.petplace.feed.entity.Tag;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.feed.repository.FeedTagRepository;
import com.minjeok4go.petplace.feed.repository.TagRepository;
import com.minjeok4go.petplace.like.repository.LikeRepository;
import com.minjeok4go.petplace.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 유저 개인의 상호작용(내 글/좋아요/댓글)을 태그 선호도 벡터로 요약하여
 * Redis 에 캐시하는 전용 서비스.
 *
 * - CBF 개인화 가산점 계산의 입력 데이터(태그 선호, 보유 동물)를 제공
 * - 추천 랭킹 엔진(CBFRecommendationService)은 여기서 읽기만 하면 됨
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CBFUserProfileService {

    /** 행동별 가중치(시작값). 필요시 @Value 또는 @ConfigurationProperties */
    private static final double W_MINE = 2.0;  // 내가 쓴 글의 태그
    private static final double W_LIKE = 2.5;  // 내가 좋아요한 글의 태그
    private static final double W_CMT  = 1.2;  // 내가 댓글 단 글의 태그

    /** Redis TTL (개인 프로필의 신선도 유지) */
    private static final Duration PROFILE_TTL = Duration.ofHours(48);

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final FeedTagRepository feedTagRepository;
    private final TagRepository tagRepository;
    private final PetRepository petRepository;
    private final FeedRepository feedRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 유저 한 명의 개인 프로필(태그 선호 벡터 + 보유 동물 세트)을 재계산하여 Redis에 저장.
     * - 읽기 전용 트랜잭션으로 DB 일관성 확보 (쓰기 작업은 Redis에만 수행)
     * - 예외는 잡아서 경고 로그로 남기고 종료(호출자에 오류 전파 X)
     */
    @Transactional(readOnly = true)
    public void buildAndCacheUserProfile(Long userId) {
        try {
            // 1) 내 글 / 좋아요 / 댓글 → 피드 ID 수집
            List<Long> myFeedIds   = safeList(feedRepository.findIdsByUserId(userId));
            List<Long> likeFeedIds = safeList(likeRepository.findFeedIdsByUserId(userId));
            List<Long> cmtFeedIds  = safeList(commentRepository.findFeedIdsByUserId(userId));

            // 2) 통합 후보 피드 집합(중복 제거)
            Set<Long> allFeedIds = new HashSet<>();
            allFeedIds.addAll(myFeedIds);
            allFeedIds.addAll(likeFeedIds);
            allFeedIds.addAll(cmtFeedIds);

            // 3) 보유 동물 세트 (상호작용 없어도 항상 최신값으로 유지)
            //    LinkedHashSet 사용 이유: 일관된 순서를 유지(주로 디버깅 가독성)
            Set<String> myAnimals = petRepository.findByUserId(userId).stream()
                    .map(p -> p.getAnimal().name())
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

            // 4) 태그 선호 점수 누적
            Map<String, Double> tagScore = new HashMap<>();
            if (!allFeedIds.isEmpty()) {
                // 4-1) (feedId, tagId) 벌크 조회 → 개별 루프에서 DB 왕복 방지
                List<FeedTagRepository.FeedTagPair> pairs =
                        feedTagRepository.findFeedTagPairsByFeedIdIn(allFeedIds);

                if (!pairs.isEmpty()) {
                    // 4-2) tagId -> tagName 매핑 (한 번에 로드)
                    Set<Long> tagIds = pairs.stream()
                            .map(FeedTagRepository.FeedTagPair::getTagId)
                            .collect(Collectors.toSet());

                    Map<Long, String> tagIdToName = tagRepository.findByIdIn(tagIds).stream()
                            .collect(Collectors.toMap(Tag::getId, Tag::getName));

                    // 4-3) 포함 여부 O(1) 체크를 위해 Set 로 변환
                    Set<Long> mineSet = new HashSet<>(myFeedIds);
                    Set<Long> likeSet = new HashSet<>(likeFeedIds);
                    Set<Long> cmtSet  = new HashSet<>(cmtFeedIds);

                    // 4-4) 태그별로 가중치 누적(한 피드가 여러 경로에 포함되면 모두 더함)
                    for (FeedTagRepository.FeedTagPair p : pairs) {
                        Long fid  = p.getFeedId();
                        String tn = tagIdToName.get(p.getTagId());
                        if (tn == null) continue; // 태그 삭제 등 방어

                        if (mineSet.contains(fid)) tagScore.merge(tn, W_MINE, Double::sum);
                        if (likeSet.contains(fid)) tagScore.merge(tn, W_LIKE, Double::sum);
                        if (cmtSet.contains(fid))  tagScore.merge(tn, W_CMT,  Double::sum);
                    }
                }
            }

            // 5) Redis 저장 (HSET: 태그/가중치, SET: 동물)
            String tagKey    = "prof:u:" + userId + ":tag";
            String animalKey = "prof:u:" + userId + ":animal";

            // 5-1) 태그 선호 벡터: 비어 있으면 기존 캐시 삭제(신선도 보장)
            if (!tagScore.isEmpty()) {
                Map<String, String> payload = new HashMap<>(tagScore.size());
                tagScore.forEach((k, v) -> payload.put(k, String.valueOf(v)));
                redisTemplate.opsForHash().putAll(tagKey, payload);
                redisTemplate.expire(tagKey, PROFILE_TTL);
            } else {
                redisTemplate.delete(tagKey);
            }

            // 5-2) 보유 동물 세트: 항상 최신 값으로 교체
            redisTemplate.delete(animalKey);
            if (!myAnimals.isEmpty()) {
                for (String a : myAnimals) {
                    redisTemplate.opsForSet().add(animalKey, a);
                }
                redisTemplate.expire(animalKey, PROFILE_TTL);
            }

            log.debug("[Profile] rebuilt user profile uid={}, tags={}, animals={}",
                    userId, tagScore.size(), myAnimals.size());

        } catch (Exception e) {
            // 비동기로도 자주 호출될 수 있으니 오염 방지를 위해 경고 로그만 남김
            log.warn("[Profile] build failed uid={}", userId, e);
        }
    }

    /**
     * 조회 시 재랭크 단계에서 사용하는 태그 선호 벡터 로더.
     * - Redis Hash("prof:u:{uid}:tag") → Map<String tagName, Double score>
     * - 빈 경우 Map.of() 반환(Null 반환 금지)
     */
    @Transactional(readOnly = true)
    public Map<String, Double> loadTagPref(Long userId) {
        var raw = redisTemplate.opsForHash().entries("prof:u:" + userId + ":tag");
        if (raw == null || raw.isEmpty()) return Map.of();

        Map<String, Double> out = new HashMap<>(raw.size());
        raw.forEach((k, v) -> {
            // value 는 문자열로 저장되어 있으므로 Double 로 파싱
            out.put((String) k, Double.parseDouble(String.valueOf(v)));
        });
        return out;
        // 주의: 여기서 DB/HDD I/O 를 추가하지 말 것(재랭크 경로 핫패스)
    }

    /**
     * 조회 시 재랭크 단계에서 사용하는 보유 동물 세트 로더.
     * - Redis Set("prof:u:{uid}:animal") → Set<String animalName>
     * - 빈 경우 Set.of() 반환
     */
    @Transactional(readOnly = true)
    public Set<String> loadAnimals(Long userId) {
        var members = redisTemplate.opsForSet().members("prof:u:" + userId + ":animal");
        return (members == null) ? Set.of() : members;
    }

    /**
     * 활성 유저 다건에 대해 프로필을 배치 갱신.
     * - 실패한 유저는 개별 try-catch 로 스킵하여 나머지에 영향 주지 않음
     * - @Async: 호출자는 즉시 반환(백그라운드 실행)
     */
    @Async
    public void refreshAllProfilesBatch(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        for (Long uid : userIds) {
            try {
                buildAndCacheUserProfile(uid);
            } catch (Exception e) {
                log.warn("[Profile] batch build failed uid={}", uid, e);
            }
        }
    }

    /**
     * 특정 유저의 프로필 캐시 무효화(탈퇴/리셋 등).
     * - 다음 조회 시 콜드 상태로 동작하며, 필요하면 build 를 재호출
     */
    public void invalidate(Long userId) {
        redisTemplate.delete("prof:u:" + userId + ":tag");
        redisTemplate.delete("prof:u:" + userId + ":animal");
    }

    /** null-safe 리스트 변환 유틸 */
    private static <T> List<T> safeList(List<T> in) {
        return (in == null) ? Collections.emptyList() : in;
    }
}
