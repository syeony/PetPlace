package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.comment.repository.CommentRepository;
import com.minjeok4go.petplace.common.constant.Animal;
import com.minjeok4go.petplace.feed.dto.FeedListResponse;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.pet.repository.PetRepository;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.region.repository.RegionRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class CBFRecommendationService {

    private final FeedRepository feedRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserGroupService userGroupService;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RegionRepository regionRepository;

    // 가중치
    private static final double WEIGHT_LIKE = 2.0;
    private static final double WEIGHT_COMMENT = 1.0;
    private static final double WEIGHT_SAME_ANIMAL_WRITER = 7.0;
    private static final double WEIGHT_NEW_FEED = 10.0;

    /**
     * 배치: 그룹별 추천 ZSET 생성 (N+1 제거, 파이프라인 적용)
     * - regions: 전체 로드 캐시
     * - users: 전체 로드 → petsByUser IN 조회
     * - feeds: 상위 200 한 번만
     * - writer info/pets: IN 조회
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public void batchRecommendationToRedis() {
        LocalDate today = LocalDate.now();

        // 1) 후보 피드 1회 조회
        List<Feed> feeds = feedRepository.findTop200ByOrderByLikesDesc();
        if (feeds.isEmpty()) return;

        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();

        // 2) 댓글 수 벌크 조회 (feedId -> count)
        List<Object[]> commentCounts =
                commentRepository.countByFeedIdInAndDeletedAtIsNullGroupByFeedId(feedIds);
        Map<Long, Integer> feedIdToCommentCount = new HashMap<>(feedIds.size());
        for (Object[] row : commentCounts) {
            feedIdToCommentCount.put((Long) row[0], ((Long) row[1]).intValue());
        }

        // 3) 작성자 정보/펫 정보 일괄 조회
        Set<Long> writerIds = feeds.stream()
                .map(Feed::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> writerMap = userRepository.findAllById(writerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // ⚠️ PetRepository에 필요: List<Pet> findByUserIdIn(Collection<Long> userIds);
        Map<Long, Set<Animal>> writerAnimals = petRepository.findByUserIdIn(writerIds).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getUser().getId(),
                        Collectors.mapping(Pet::getAnimal, Collectors.toSet())
                ));

        // 4) region 캐시 (id -> name)
        Map<Long, String> regionNameCache = regionRepository.findAll().stream()
                .collect(Collectors.toMap(Region::getId, Region::getName));

        // 5) 모든 사용자 + 사용자별 펫을 IN 조회로 미리 로드 (N+1 제거)
        List<User> users = userRepository.findAll();
        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, List<Pet>> petsByUser = petRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(p -> p.getUser().getId()));

        // 6) 사용자별 점수 계산 → Redis 파이프라인으로 저장
        for (User user : users) {
            List<Pet> pets = petsByUser.getOrDefault(user.getId(), Collections.emptyList());

            // 그룹키 생성 시 캐시 리졸버 사용 (regions N+1 방지)
            String groupKey = userGroupService.determineGroupKey(
                    user, pets, id -> regionNameCache.getOrDefault(id, "UNKNOWN")
            );
            String redisKey = "group:" + groupKey;

            // 유저 나이대/지역 미리 계산
            int userAge = Period.between(user.getBirthday(), today).getYears();
            int userAgeGroup = (userAge / 10) * 10;
            String userRegionName = regionNameCache.getOrDefault(user.getRegionId(), "UNKNOWN");

            Map<Long, Double> scores = new HashMap<>(feedIds.size());

            for (Feed feed : feeds) {
                int commentCount = feedIdToCommentCount.getOrDefault(feed.getId(), 0);

                // 작성자 캐시
                User writer = (feed.getUserId() == null) ? null : writerMap.get(feed.getUserId());
                int writerAgeGroup = -1;
                String writerRegionName = null;
                Set<Animal> writerAnimalSet = Collections.emptySet();

                if (writer != null) {
                    int writerAge = Period.between(writer.getBirthday(), today).getYears();
                    writerAgeGroup = (writerAge / 10) * 10;
                    writerRegionName = regionNameCache.getOrDefault(writer.getRegionId(), "UNKNOWN");
                    writerAnimalSet = writerAnimals.getOrDefault(writer.getId(), Collections.emptySet());
                }

                double score = calculateScore(
                        user, pets, feed, commentCount,
                        writerAgeGroup, writerRegionName, writerAnimalSet,
                        userAgeGroup, userRegionName,
                        today
                );
                scores.put(feed.getId(), score);
            }

            // Redis 파이프라인으로 대량 ZADD
            redisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                var keySer = redisTemplate.getStringSerializer();
                byte[] key = keySer.serialize(redisKey);
                for (Map.Entry<Long, Double> e : scores.entrySet()) {
                    conn.zAdd(key, e.getValue(), keySer.serialize(String.valueOf(e.getKey())));
                }
                return null;
            });
        }
    }

    private double calculateScore(
            User user, List<Pet> pets, Feed feed, int commentCount,
            int writerAgeGroup, String writerRegionName, Set<Animal> writerAnimalSet,
            int userAgeGroup, String userRegionName,
            LocalDate today
    ) {
        double score = 0;

        score += feed.getLikes() * WEIGHT_LIKE;
        score += commentCount * WEIGHT_COMMENT;

        if (!pets.isEmpty() && writerAnimalSet != null && !writerAnimalSet.isEmpty()) {
            boolean sameAnimalWithWriter = pets.stream()
                    .anyMatch(myPet -> writerAnimalSet.contains(myPet.getAnimal()));
            if (sameAnimalWithWriter) score += WEIGHT_SAME_ANIMAL_WRITER;
        }

        if (writerAgeGroup != -1 && writerAgeGroup == userAgeGroup) score += 5;
        if (writerRegionName != null && writerRegionName.equals(userRegionName)) score += 5;

        if (feed.getCreatedAt() != null &&
                feed.getCreatedAt().isAfter(today.minusDays(2).atStartOfDay())) {
            score += WEIGHT_NEW_FEED;
        }

        return score;
    }

    // 조회 API는 그대로 (필요시 유지)
    public List<FeedListResponse> getRecommendedFeeds(User user, int page, int size) {
        List<Pet> pets = petRepository.findByUserId(user.getId());
        String groupKey = userGroupService.determineGroupKey(user, pets);

        String redisKey = "group:" + groupKey;
        long start = (long) page * size;
        long end = start + size - 1;

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, start, end);
        if (tuples == null || tuples.isEmpty()) return Collections.emptyList();

        List<Long> feedIdsInOrder = tuples.stream()
                .map(t -> Long.parseLong(Objects.requireNonNull(t.getValue())))
                .toList();

        Map<Long, Double> scoreById = tuples.stream()
                .collect(Collectors.toMap(
                        t -> Long.parseLong(Objects.requireNonNull(t.getValue())),
                        t -> Optional.ofNullable(t.getScore()).orElse(0.0)
                ));

        List<Feed> feeds = feedRepository.findAllById(feedIdsInOrder);
        Map<Long, Feed> feedById = feeds.stream()
                .collect(Collectors.toMap(Feed::getId, Function.identity()));

        List<FeedListResponse> out = new ArrayList<>(feedIdsInOrder.size());
        for (Long id : feedIdsInOrder) {
            Feed f = feedById.get(id);
            if (f != null) out.add(FeedListResponse.from(f, scoreById.getOrDefault(id, 0.0)));
        }
        return out;
    }
    // CBFRecommendationService.java

//    @Async
//    public void batchRecommendationToRedisAsync() {
//        long t0 = System.currentTimeMillis();
//        batchRecommendationToRedis();
//        log.info("recommend/batch finished in {} ms", System.currentTimeMillis() - t0);
//    }
//}
@Async("recommendationExecutor")
public void batchRecommendationToRedisAsync() {
    final long batchStartMs = System.currentTimeMillis();

    long cpuNsTotal = 0L;   // 계산 구간 누적(ns)
    long ioNsTotal  = 0L;   // Redis 쓰기 구간 누적(ns)
    long itemsTotal = 0L;   // ZADD(또는 저장)한 총 아이템 수

    try {
        LocalDate today = LocalDate.now();

        // 1) 후보 피드 1회 조회
        List<Feed> feeds = feedRepository.findTop200ByOrderByLikesDesc();
        if (feeds.isEmpty()) {
            log.info("CBF batch: no feeds, skip");
            return;
        }
        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();

        // 2) 댓글 수 벌크 조회
        List<Object[]> commentCounts =
                commentRepository.countByFeedIdInAndDeletedAtIsNullGroupByFeedId(feedIds);
        Map<Long, Integer> feedIdToCommentCount = new HashMap<>(feedIds.size());
        for (Object[] row : commentCounts) {
            feedIdToCommentCount.put((Long) row[0], ((Long) row[1]).intValue());
        }

        // 3) 작성자 정보/펫 정보 일괄 조회
        Set<Long> writerIds = feeds.stream()
                .map(Feed::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> writerMap = userRepository.findAllById(writerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, Set<Animal>> writerAnimals = petRepository.findByUserIdIn(writerIds).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getUser().getId(),
                        Collectors.mapping(Pet::getAnimal, Collectors.toSet())
                ));

        // 4) region 캐시
        Map<Long, String> regionNameCache = regionRepository.findAll().stream()
                .collect(Collectors.toMap(Region::getId, Region::getName));

        // 5) 모든 사용자 + 사용자별 펫 미리 로드
        List<User> users = userRepository.findAll();
        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, List<Pet>> petsByUser = petRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(p -> p.getUser().getId()));

        // 6) 사용자별 점수 계산(=CPU) → Redis 파이프라인 저장(=I/O)
        for (User user : users) {
            List<Pet> pets = petsByUser.getOrDefault(user.getId(), Collections.emptyList());

            String groupKey = userGroupService.determineGroupKey(
                    user, pets, id -> regionNameCache.getOrDefault(id, "UNKNOWN")
            );
            String redisKey = "group:" + groupKey;

            int userAge = Period.between(user.getBirthday(), today).getYears();
            int userAgeGroup = (userAge / 10) * 10;
            String userRegionName = regionNameCache.getOrDefault(user.getRegionId(), "UNKNOWN");

            // ---- CPU 구간 시작: 점수 계산 ----
            long t0 = System.nanoTime();

            Map<Long, Double> scores = new HashMap<>(feedIds.size());
            for (Feed feed : feeds) {
                int commentCount = feedIdToCommentCount.getOrDefault(feed.getId(), 0);

                User writer = (feed.getUserId() == null) ? null : writerMap.get(feed.getUserId());
                int writerAgeGroup = -1;
                String writerRegionName = null;
                Set<Animal> writerAnimalSet = Collections.emptySet();

                if (writer != null) {
                    int writerAge = Period.between(writer.getBirthday(), today).getYears();
                    writerAgeGroup = (writerAge / 10) * 10;
                    writerRegionName = regionNameCache.getOrDefault(writer.getRegionId(), "UNKNOWN");
                    writerAnimalSet = writerAnimals.getOrDefault(writer.getId(), Collections.emptySet());
                }

                double score = calculateScore(
                        user, pets, feed, commentCount,
                        writerAgeGroup, writerRegionName, writerAnimalSet,
                        userAgeGroup, userRegionName,
                        today
                );
                scores.put(feed.getId(), score);
            }

            long t1 = System.nanoTime();
            cpuNsTotal += (t1 - t0);
            // ---- CPU 구간 끝 ----

            // ---- I/O 구간 시작: Redis 파이프라인 쓰기 ----
            long t2 = System.nanoTime();

            final var keySer = redisTemplate.getStringSerializer();
            final byte[] key = keySer.serialize(redisKey);
            final long[] written = {0L};

            redisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                for (Map.Entry<Long, Double> e : scores.entrySet()) {
                    conn.zAdd(key, e.getValue(), keySer.serialize(String.valueOf(e.getKey())));
                    written[0]++;
                }
                return null;
            });

            itemsTotal += written[0];

            long t3 = System.nanoTime();
            ioNsTotal += (t3 - t2);
            // ---- I/O 구간 끝 ----
        }

        long n = Math.max(1L, itemsTotal);
        double C = (cpuNsTotal / 1_000_000.0) / n; // ms/건
        double W = (ioNsTotal  / 1_000_000.0) / n; // ms/건
        log.info("[CBF Timing] avgCPU={} ms, avgIO={} ms, W/C={}, items={}", C, W, (W / C), itemsTotal);

    } catch (Exception e) {
        log.error("CBF batch failed", e);
    } finally {
        log.info("recommend/batch finished in {} ms", System.currentTimeMillis() - batchStartMs);
    }
}

}
