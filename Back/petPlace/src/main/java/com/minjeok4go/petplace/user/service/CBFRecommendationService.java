package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.comment.repository.CommentRepository;
import com.minjeok4go.petplace.common.constant.Animal;
import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.feed.dto.FeedDetailResponse;
import com.minjeok4go.petplace.feed.dto.FeedListResponse;
import com.minjeok4go.petplace.feed.dto.FeedTagJoin;
import com.minjeok4go.petplace.feed.dto.TagResponse;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.feed.repository.FeedRepository;
import com.minjeok4go.petplace.feed.repository.FeedTagRepository;
import com.minjeok4go.petplace.feed.repository.TagRepository;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.like.repository.LikeRepository;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.pet.repository.PetRepository;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.region.repository.RegionRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final LikeRepository likeRepository;
    private final RecommendationCacheService recommendationCacheService;

    // 개인 프로필(태그/동물) 읽기용
    private final CBFUserProfileService userProfileService;

    // 피드 ↔ 태그 매핑 읽기용
    private final FeedTagRepository feedTagRepository;
    private final TagRepository tagRepository;

    // 가중치
    private static final double WEIGHT_LIKE = 20.0;
    private static final double WEIGHT_COMMENT = 15.0;
    private static final double WEIGHT_SAME_ANIMAL_WRITER = 24.0;
    private static final double WEIGHT_NEW_FEED = 10.0;
    private final ImageRepository imageRepository;

    /**
     * 배치: 그룹별 추천 ZSET 생성 (N+1 제거, 파이프라인 적용)
     * - regions: 전체 로드 캐시
     * - users: 전체 로드 → petsByUser IN 조회
     * - feeds: 상위 200 한 번만
     * - writer info/pets: IN 조회
     */
    @PostConstruct
    public void proveCommentCountType() throws Exception {
        var f = FeedDetailResponse.class.getDeclaredField("commentCount");
        log.warn("[PROVE] commentCount type = {}", f.getType().getName()); // int or java.lang.Integer
    }
    /**
     * 후보 피드들에 대해 유저 개인의 태그/동물 선호 기반 가산점을 계산한다.
     * - tagPref: Redis Hash("prof:u:{uid}:tag")에서 읽은 {tagName -> score}
     * - myAnimals: Redis Set("prof:u:{uid}:animal")에서 읽은 동물명 집합
     * - feedTags: 후보 feedId -> 태그명 목록
     */

    private Map<Long, Double> computePersonalCbfBoost(Long userId, List<Long> candidateIds) {
        // 1) 유저 개인 프로필 로딩 (빈 경우 빠르게 반환)
        Map<String, Double> tagPref = userProfileService.loadTagPref(userId);
        Set<String> myAnimals = userProfileService.loadAnimals(userId);
        boolean hasPrefs = (tagPref != null && !tagPref.isEmpty()) || (myAnimals != null && !myAnimals.isEmpty());
        if (!hasPrefs || candidateIds == null || candidateIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2) 후보 피드의 (feedId, tagId) 일괄 로딩 → tagId -> tagName 매핑
        // --- 권장: FeedTagRepository에 프로젝션 메서드(findFeedTagPairsByFeedIdIn)가 있을 때 ---
        Map<Long, List<String>> feedTags = new HashMap<>();
        List<FeedTagRepository.FeedTagPair> pairs =
                feedTagRepository.findFeedTagPairsByFeedIdIn(candidateIds);

        if (pairs != null && !pairs.isEmpty()) {
            Set<Long> tagIds = pairs.stream()
                    .map(FeedTagRepository.FeedTagPair::getTagId)
                    .collect(Collectors.toSet());

            Map<Long, String> tagIdToName = tagRepository.findByIdIn(tagIds).stream()
                    .collect(Collectors.toMap(com.minjeok4go.petplace.feed.entity.Tag::getId,
                            com.minjeok4go.petplace.feed.entity.Tag::getName));

            for (FeedTagRepository.FeedTagPair p : pairs) {
                String name = tagIdToName.get(p.getTagId());
                if (name != null) {
                    feedTags.computeIfAbsent(p.getFeedId(), k -> new ArrayList<>()).add(name);
                }
            }
        } else {
            // --- 대안: 프로젝션이 없다면 (후보 수가 작을 때만) per-feed 조회로 대체 ---
            for (Long fid : candidateIds) {
                List<Long> tagIds = feedTagRepository.findTagIdByFeedId(fid);
                if (tagIds == null || tagIds.isEmpty()) continue;
                var tags = tagRepository.findByIdIn(tagIds).stream()
                        .map(com.minjeok4go.petplace.feed.entity.Tag::getName)
                        .toList();
                if (!tags.isEmpty()) feedTags.put(fid, new ArrayList<>(tags));
            }
        }

        // 3) 개인 가산점 계산
        final double W_ANIMAL = 2.0; // 동물 매칭 시 가산치(튜닝 포인트)
        Map<Long, Double> boost = new HashMap<>(candidateIds.size());

        for (Long fid : candidateIds) {
            double personal = 0.0;

            // 3-1) 태그 선호 합
            for (String tag : feedTags.getOrDefault(fid, List.of())) {
                personal += tagPref.getOrDefault(tag, 0.0);
            }

            // 3-2) (옵션) 동물 매칭 가산
            if (myAnimals != null && !myAnimals.isEmpty()) {
                for (String t : feedTags.getOrDefault(fid, List.of())) {
                    if (myAnimals.contains(t)) { personal += W_ANIMAL; break; }
                }
            }

            if (personal != 0.0) boost.put(fid, personal);
        }
        return boost;
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
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

    public List<FeedListResponse> getRecommendedFeeds(Long userId, int page, int size) {
        if (userId == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        // 0) 그룹 키 산출 + 유저/펫 로드
        List<Pet> pets = petRepository.findByUserId(userId);
        User user = userRepository.getReferenceById(userId);
        String groupKey = userGroupService.determineGroupKey(user, pets);

        String redisKey = "group:" + groupKey;

        // === [A] Redis ZSET에서 "순서 힌트"로 후보 ID를 빠르게 가져오기 (오버샘플: size * 3) ===
        long start = (long) page * size;
        long fetch = size * 3L; // 삭제/핀고정/재정렬로 빠지는 걸 대비
        long end   = start + fetch - 1;

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, start, end);

        List<Long> candidateIds = new ArrayList<>();
        Map<Long, Double> zsetScore = new HashMap<>(); // 배치 시점 점수(순서 힌트)
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                if (t == null || t.getValue() == null) continue;
                long fid = Long.parseLong(t.getValue());
                candidateIds.add(fid);
                zsetScore.put(fid, Optional.ofNullable(t.getScore()).orElse(1.0));
            }
        }

        // 캐시 비어있을 때 안전망: DB Top200 (라이브)
        if (candidateIds.isEmpty()) {
            List<Feed> fallback = feedRepository.findTop200ByOrderByLikesDesc(); // 반드시 deletedAt 제외/보조정렬 포함 권장
            candidateIds = fallback.stream().map(Feed::getId).toList();
            for (Long id : candidateIds) zsetScore.put(id, 1.0);
        }

        // === [B] 내 최근 글(3시간 내 최대 3개) 핀고정 후보 추가 ===
        LocalDateTime threeMinutesAgo = LocalDateTime.now().minusMinutes(3);
        List<Long> myRecentFeedIds = feedRepository
                .findTop3IdsByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, threeMinutesAgo);

        // === [C] 1차 풀 구성(핀고정 우선 + 후보) → 중복 제거 후 오버샘플 크기 제한 ===
        List<Long> pool = Stream.concat(myRecentFeedIds.stream(), candidateIds.stream())
                .distinct()
                .limit(fetch)
                .toList();
        if (pool.isEmpty()) return List.of();

        // === [D] 실시간 정합성 오버레이 ===
        // D-1) 살아있는 글만 남기기(soft delete 제외) + 지워진 멤버는 Redis에서 lazy-clean
        List<Feed> alive = feedRepository.findAllActiveByIdIn(pool);
        Map<Long, Feed> aliveById = alive.stream().collect(Collectors.toMap(Feed::getId, Function.identity()));

        List<Object> deadMembers = pool.stream()
                .filter(id -> !aliveById.containsKey(id))
                .map(String::valueOf)
                .map(Object.class::cast)
                .toList();
        if (!deadMembers.isEmpty()) {
            redisTemplate.opsForZSet().remove(redisKey, deadMembers.toArray(new Object[0]));
        }
        // 실존 ID만 대상으로 후속 계산
        List<Long> liveIds = pool.stream().filter(aliveById::containsKey).toList();
        if (liveIds.isEmpty()) return List.of();

        // D-2) 현재 DB 상태로 "배치 점수"를 미니 재계산(좋아요/댓글/최신성/작성자 특성 반영)
        //      - 윈도우 내에서만 계산하므로 비용 작음
        // 댓글 수(삭제 제외) 벌크 조회
//        List<Object[]> commentCountsRows =
//                commentRepository.countByFeedIdInAndDeletedAtIsNullGroupByFeedId(liveIds);
//        Map<Long, Integer> commentCount = new HashMap<>(liveIds.size());
//        for (Object[] row : commentCountsRows) {
//            commentCount.put((Long) row[0], ((Long) row[1]).intValue());
//        }
//
        List<Object[]> rows =
                commentRepository.countByFeedIdInAndDeletedAtIsNullGroupByFeedId(liveIds);

        Map<Long, Integer> commentCount = new HashMap<>(liveIds.size());
        for (Object[] row : rows) {
            Long fid = ((Number) row[0]).longValue();
            int cnt  = ((Number) row[1]).intValue();
            commentCount.put(fid, cnt);
        }
        // 작성자/작성자 동물
        Set<Long> writerIds = alive.stream()
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

        // 지역명 resolve(필요한 regionId만)
        Set<Long> regionIds = new HashSet<>();
        for (Feed f : alive) {
            if (f.getRegionId() != null) regionIds.add(f.getRegionId());
        }
        if (user.getRegionId() != null) regionIds.add(user.getRegionId());
        Map<Long, String> regionNameCache = regionRepository.findAllById(regionIds).stream()
                .collect(Collectors.toMap(Region::getId, Region::getName));

        // 유저 나이대/지역명
        LocalDate today = LocalDate.now();
        int userAge = Period.between(user.getBirthday(), today).getYears();
        int userAgeGroup = (userAge / 10) * 10;
        String userRegionName = regionNameCache.getOrDefault(user.getRegionId(), "UNKNOWN");

        // 현재 상태로 기본 점수 재계산
        Map<Long, Double> rtBaseScore = new HashMap<>(liveIds.size());
        for (Long fid : liveIds) {
            Feed f = aliveById.get(fid);
            int cmtCnt = commentCount.getOrDefault(fid, 0);

            User writer = (f.getUserId() == null) ? null : writerMap.get(f.getUserId());
            int writerAgeGroup = -1;
            String writerRegionName = null;
            Set<Animal> writerAnimalSet = Collections.emptySet();

            if (writer != null) {
                int writerAge = Period.between(writer.getBirthday(), today).getYears();
                writerAgeGroup = (writerAge / 10) * 10;
                writerRegionName = regionNameCache.getOrDefault(writer.getRegionId(), "UNKNOWN");
                writerAnimalSet = writerAnimals.getOrDefault(writer.getId(), Collections.emptySet());
            }

            double s = calculateScore(
                    user, pets, f, cmtCnt,
                    writerAgeGroup, writerRegionName, writerAnimalSet,
                    userAgeGroup, userRegionName,
                    today
            );
            rtBaseScore.put(fid, s);
        }

        // === [E] 개인 CBF 가산점 적용(프로필 캐시 기반) ===
        Map<Long, Double> cbfBoost = computePersonalCbfBoost(userId, liveIds);
        double ALPHA = 0.5;

        Map<Long, Double> finalScoreById = new HashMap<>(liveIds.size());
        for (Long id : liveIds) {
            double base = rtBaseScore.getOrDefault(id, zsetScore.getOrDefault(id, 1.0)); // 재계산 점수 우선, 없으면 배치 점수
            double cbf  = cbfBoost.getOrDefault(id, 0.0);
            finalScoreById.put(id, base + ALPHA * cbf);
        }

        // === [F] 최종 정렬: "내 최근 글"을 최상단 고정, 나머지는 최종 점수로 내림차순 ===
        Set<Long> pinSet = new LinkedHashSet<>(myRecentFeedIds); // 순서 유지
        List<Long> sortedByScore = liveIds.stream()
                .sorted(Comparator.comparingDouble(id -> finalScoreById.getOrDefault(id, 0.0)).reversed())
                .toList();

        List<Long> ordered = new ArrayList<>((int) fetch);
        // 1) 핀 고정
        for (Long id : myRecentFeedIds) {
            if (aliveById.containsKey(id)) ordered.add(id);
            if (ordered.size() >= fetch) break;
        }
        // 2) 점수 정렬(핀 제외)
        for (Long id : sortedByScore) {
            if (pinSet.contains(id)) continue;
            ordered.add(id);
            if (ordered.size() >= fetch) break;
        }

        // === [G] 페이지 사이즈만큼 잘라서 반환 준비 ===
        List<Long> finalIds = ordered.stream().limit(size).toList();
        if (finalIds.isEmpty()) return List.of();

        // === [H] HYDRATE (이미지/태그/좋아요 여부) ===
        List<Feed> feeds = feedRepository.findAllById(finalIds);
        Map<Long, Feed> feedById = feeds.stream().collect(Collectors.toMap(Feed::getId, Function.identity()));

        List<Image> images = imageRepository.findAllByRefTypeAndRefIdInOrderBySortAsc(RefType.FEED, finalIds);
        Map<Long, List<ImageResponse>> imagesByFeed = images.stream()
                .collect(Collectors.groupingBy(
                        Image::getRefId,
                        Collectors.mapping(ImageResponse::new, Collectors.toList())
                ));

        List<FeedTagJoin> tagRows = feedTagRepository.findAllByFeedIdIn(finalIds);
        Map<Long, List<TagResponse>> tagsByFeed = tagRows.stream()
                .collect(Collectors.groupingBy(FeedTagJoin::getFeedId,
                        Collectors.mapping(r -> new TagResponse(r.getTagId(), r.getTagName()), Collectors.toList())));

        Set<Long> likedIds = likeRepository.findFeedIdsLikedByUser(userId, finalIds);

        List<FeedListResponse> out = new ArrayList<>(finalIds.size());
        for (Long id : finalIds) {
            Feed f = feedById.get(id);
            if (f == null) continue;

            double finalScore = finalScoreById.getOrDefault(id, 1.0);
            List<ImageResponse> imgs = imagesByFeed.getOrDefault(id, List.of());
            List<TagResponse> tags  = tagsByFeed.getOrDefault(id, List.of());
            boolean liked           = likedIds.contains(id);
            int cmtCnt              = commentCount.getOrDefault(id, 0); // ✅ 이미 위에서 벌크로 뽑아놨던 그 값


//            FeedListResponse dto = FeedListResponse.from(f, finalScore, cmtCnt);
//            dto.setCommentCount(cmtCnt);
//            dto.setTags(tags);
//            dto.setImages(imgs);
//            dto.setLiked(liked);
//            out.add(dto);
            FeedListResponse dto = new FeedListResponse();
            dto.setId(f.getId());
            dto.setContent(f.getContent());
            dto.setUserId(f.getUserId());
            dto.setUserNick(f.getUserNick());
            dto.setUserImg(f.getUserImg());
            dto.setRegionId(f.getRegionId());
            dto.setCategory(f.getCategory().getDisplayName());
            dto.setCreatedAt(f.getCreatedAt());
            dto.setUpdatedAt(f.getUpdatedAt());
            dto.setDeletedAt(f.getDeletedAt());
            dto.setLikes(f.getLikes());
            dto.setViews(f.getViews());
            dto.setScore(finalScore);
            dto.setCommentCount(cmtCnt);
            dto.setTags(tags);
            dto.setImages(imgs);
            dto.setLiked(liked);
            out.add(dto);
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
// ===================== C/W 측정 가능한 @Async 배치 =====================
// =====================================================================
// [비동기 배치] 그룹별 추천 랭킹(ZSET) 미리 계산하여 Redis에 저장 + C/W(계산 vs I/O) 시간 측정
//  - 후보 풀: 좋아요 상위 200개 피드
//  - N+1 제거: 필요한 모든 부가정보(댓글수/작성자/펫/지역/유저펫)를 한 번에 벌크 로딩 → 인메모리 Map 캐시로 사용
//  - 그룹핑: userGroupService.determineGroupKey(...) 에서 groupKey를 생성 (예: 나이대/지역/동물조합 등)
//  - 저장: group:{groupKey} 라는 ZSET 키에 (member=feedId, score=추천점수)로 ZADD (파이프라이닝)
//  - 타이밍: CPU(계산) 구간과 I/O(레디스 저장) 구간을 분리 측정하여 평균(ms/건)과 W/C 비율 로그 출력
//  - @Async: 호출자는 즉시 반환, 실제 배치는 백그라운드 스레드에서 수행
// =====================================================================
    @Scheduled(cron ="0 0 3 * * *", zone = "Asia/Seoul") // 새벽 3시마다 배치실행
    @Async("recommendationExecutor") // executor 빈 이름을 등록했을 때. 없다면 @Async 만 사용해도 됨.
    public void batchRecommendationToRedisAsync() {

        // ============================================================
        // [동시 실행 방지 락]
        // - 이미 다른 인스턴스/스레드가 배치 실행 중이면 스킵
        // - TTL(30분)로 비정상 종료 시에도 자동 해제되게 함
        // ============================================================
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent("cbf_batch_lock", "1", java.time.Duration.ofMinutes(30));
        if (Boolean.FALSE.equals(locked)) {
            log.warn("[CBF Batch] 이미 실행 중이어서 스킵합니다.");
            return;
        }
        final long batchStartMs = System.currentTimeMillis();

        long cpuNsTotal = 0L;   // 전체 사용자 처리 동안의 "계산(CPU) 구간" 누적 시간(ns)
        long ioNsTotal  = 0L;   // 전체 사용자 처리 동안의 "I/O(레디스 저장) 구간" 누적 시간(ns)
        long itemsTotal = 0L;   // 전체 저장된 아이템 개수(ZADD 횟수). 평균 계산을 위한 분모로 사용

        try {
            // ===== 전처리(공통 캐시 구성) : 배치 한 번 도는 동안만 유효한 '인메모리 캐시'입니다. =====
            // 여기서 말하는 "캐시"는 Redis 캐시가 아니라, N+1 방지용으로 메모리에 들고 있는 Map을 뜻합니다.
            final LocalDate today = LocalDate.now();

            // (1) 후보 피드 풀: 좋아요 상위 200개만 한 번에 뽑아, 이후 계산 범위를 제한합니다.
            final List<Feed> feeds = feedRepository.findTop200ByOrderByLikesDesc();
            if (feeds.isEmpty()) {
                log.info("CBF batch: no feeds, skip");
                return; // 후보가 없으면 아무 것도 하지 않고 종료
            }
            final List<Long> feedIds = feeds.stream().map(Feed::getId).toList();

            // (2) 댓글수 벌크 조회 → feedId -> commentCount 맵 구성 (그룹핑 쿼리 결과를 메모리에 저장)
            final List<Object[]> commentCounts =
                    commentRepository.countByFeedIdInAndDeletedAtIsNullGroupByFeedId(feedIds);
            final Map<Long, Integer> feedIdToCommentCount = new HashMap<>(feedIds.size());
            for (Object[] row : commentCounts) {
                // row[0]=feedId(Long), row[1]=count(Long) 가정
                feedIdToCommentCount.put((Long) row[0], ((Long) row[1]).intValue());
            }

            // (3) 작성자(User)와 작성자의 반려동물(Animal set) 정보를 한 번에 로딩
            final Set<Long> writerIds = feeds.stream()
                    .map(Feed::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // writerId -> User
            final Map<Long, User> writerMap = userRepository.findAllById(writerIds).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));

            // writerId -> {Animal...} : PetRepository에는 findByUserIdIn(...) 이 있어야 함
            final Map<Long, Set<Animal>> writerAnimals = petRepository.findByUserIdIn(writerIds).stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getUser().getId(),
                            Collectors.mapping(Pet::getAnimal, Collectors.toSet())
                    ));

            // (4) 지역 전체 로딩 → regionId -> regionName 맵 (이름 resolve 용)
            final Map<Long, String> regionNameCache = regionRepository.findAll().stream()
                    .collect(Collectors.toMap(Region::getId, Region::getName));

            // (5) 모든 사용자와 각 사용자의 반려동물을 한 번에 로딩하여 맵 구성 (userId -> pets[])
            final List<User> users = userRepository.findAll();
            final List<Long> userIds = users.stream().map(User::getId).toList();
            final Map<Long, List<Pet>> petsByUser = petRepository.findByUserIdIn(userIds).stream()
                    .collect(Collectors.groupingBy(p -> p.getUser().getId()));

            // ===== 메인 루프: 사용자별로 "그룹키 생성 → 점수 계산 → Redis 저장" =====
            for (User user : users) {
                final List<Pet> pets = petsByUser.getOrDefault(user.getId(), Collections.emptyList());

                // [그룹핑] 여기서 groupKey를 생성합니다. (이 단계가 "그룹핑"의 실체)
                // 예: "10s:Seoul:Dog" 형식 등. 실제 키는 userGroupService 구현에 따름.
                final String groupKey = userGroupService.determineGroupKey(
                        user, pets, id -> regionNameCache.getOrDefault(id, "UNKNOWN")
                );

                // Redis ZSET 키 네임스페이스: "group:{groupKey}"
                // 같은 그룹의 사용자라면 동일한 키를 참조합니다.
                final String redisKey = "group:" + groupKey;

                // --- [CPU 구간] 점수 계산 시작 ---
                final long t0 = System.nanoTime();

                // 사용자 1명에 대해, 후보 200개 피드 각각의 점수를 계산하여 feedId -> score 맵 생성
                final Map<Long, Double> scores = computeScoresForUser(
                        user, pets, feeds, feedIdToCommentCount,
                        writerMap, writerAnimals, regionNameCache, today
                );

                final long t1 = System.nanoTime();
                cpuNsTotal += (t1 - t0); // 계산 구간 누적
                // --- [CPU 구간] 종료 ---

                // --- [I/O 구간] Redis 저장 시작 ---
                final long t2 = System.nanoTime();

                // 점수 맵을 레디스 ZSET에 파이프라이닝으로 ZADD.
                // member=feedId, score=추천점수 → 높은 점수일수록 상단 노출.
                // 반환값은 ZADD한 총 건수 (itemsTotal에 누적)
                itemsTotal += writeScoresToRedis(redisKey, scores);

                // 추가: 이 그룹에 어떤 feedId들이 들어갔는지 기록
                for (Long fid : scores.keySet()) {
                    recommendationCacheService.rememberMembership(fid, groupKey);
                }

                final long t3 = System.nanoTime();
                ioNsTotal += (t3 - t2); // I/O 구간 누적
                // --- [I/O 구간] 종료 ---

                // [선택] 원자적 공개를 원하면 "임시키에 쌓고 → RENAME으로 스왑" 패턴을 쓰세요.
                // ex)
                // String tmpKey = "group:" + groupKey + ":tmp:" + System.currentTimeMillis();
                // writeScoresToRedis(tmpKey, scores);
                // conn.rename(tmpKey, redisKey);  // 이러면 중간 상태 노출 없이 스왑 가능
            }

            // ===== 최종 요약 로그: 평균 계산/저장 시간 및 W/C 비율 =====
            final long n = Math.max(1L, itemsTotal); // 0으로 나누기 방지
            final double C = (cpuNsTotal / 1_000_000.0) / n; // 건당 평균 CPU 시간(ms)
            final double W = (ioNsTotal  / 1_000_000.0) / n; // 건당 평균 I/O 시간(ms)
            log.info("[CBF Timing] avgCPU={} ms, avgIO={} ms, W/C={}, items={}", C, W, (W / C), itemsTotal);

        } catch (Exception e) {
            // 비동기 메서드(void) 예외는 호출자가 못 받으니, 여기서 반드시 로깅
            log.error("CBF batch failed", e);
        } finally {
            log.info("recommend/batch finished in {} ms", System.currentTimeMillis() - batchStartMs);
            // ============================================================
            // [락 해제] - 정상/예외와 관계없이 반드시 해제
            // ============================================================
            try { redisTemplate.delete("cbf_batch_lock"); } catch (Exception ignore) {}

        }
    }

    /**
     * [순수 계산 단계] 사용자 1명에 대해 후보 피드들의 추천 점수를 계산하여 feedId -> score 맵을 만듭니다.
     *  - I/O 호출을 절대 넣지 마세요. (이 메서드는 "CPU 구간" 시간 측정을 위한 순수 계산 전용)
     *  - 가중치 합산 로직은 calculateScore(...)에 캡슐화되어 있음 (이미 네 코드에 존재)
     */
    private Map<Long, Double> computeScoresForUser(
            User user,
            List<Pet> pets,
            List<Feed> feeds,
            Map<Long, Integer> feedIdToCommentCount, // feedId -> 댓글 수
            Map<Long, User> writerMap,               // 작성자 id -> User
            Map<Long, Set<Animal>> writerAnimals,    // 작성자 id -> 작성자의 동물 종 집합
            Map<Long, String> regionNameCache,       // regionId -> regionName (표기용/동등비교용)
            LocalDate today
    ) {
        // 사용자 나이/지역을 미리 계산 (반복에서 재계산 방지)
        final int userAge = Period.between(user.getBirthday(), today).getYears();
        final int userAgeGroup = (userAge / 10) * 10; // 20대/30대...
        final String userRegionName = regionNameCache.getOrDefault(user.getRegionId(), "UNKNOWN");

        final Map<Long, Double> scores = new HashMap<>(feeds.size());

        // 후보 200개 피드를 하나씩 돌며 점수 합산
        for (Feed feed : feeds) {
            final int commentCount = feedIdToCommentCount.getOrDefault(feed.getId(), 0);

            // 피드 작성자 데이터 resolve (없을 수도 있음)
            final User writer = (feed.getUserId() == null) ? null : writerMap.get(feed.getUserId());
            int writerAgeGroup = -1;
            String writerRegionName = null;
            Set<Animal> writerAnimalSet = Collections.emptySet();

            if (writer != null) {
                final int writerAge = Period.between(writer.getBirthday(), today).getYears();
                writerAgeGroup = (writerAge / 10) * 10;
                writerRegionName = regionNameCache.getOrDefault(writer.getRegionId(), "UNKNOWN");
                writerAnimalSet = writerAnimals.getOrDefault(writer.getId(), Collections.emptySet());
            }

            // 실제 가중치 합산 로직(좋아요/댓글/동물 일치/나이대/지역/최신성 등)은 calculateScore(...)로 캡슐화
            final double score = calculateScore(
                    user, pets, feed, commentCount,
                    writerAgeGroup, writerRegionName, writerAnimalSet,
                    userAgeGroup, userRegionName,
                    today
            );
            scores.put(feed.getId(), score);
        }
        return scores;
    }

    /**
     * [저장 단계] feedId -> score 맵을 Redis ZSET에 파이프라이닝으로 저장합니다.
     *  - 키: group:{groupKey}
     *  - member: feedId (문자열)
     *  - score: 추천 점수(더 높을수록 랭킹 상단)
     *  - 성능 팁: 파이프라이닝은 네트워크 RTT를 크게 줄여주므로, 스레드만 늘리는 것보다 효과적입니다.
     * @return 실제로 ZADD한 개수 (itemsTotal에 누적하여 평균 산출에 사용)
     */
    private long writeScoresToRedis(String redisKey, Map<Long, Double> scores) {
        final var keySer = redisTemplate.getStringSerializer();
        final byte[] key = keySer.serialize(redisKey);
        final long[] written = {0L};

        // 파이프라이닝으로 다건 ZADD
        redisTemplate.executePipelined((RedisCallback<Object>) conn -> {
            for (Map.Entry<Long, Double> e : scores.entrySet()) {
                // member는 feedId 문자열, score는 계산된 점수
                conn.zAdd(key, e.getValue(), keySer.serialize(String.valueOf(e.getKey())));
                written[0]++;
            }
            conn.expire(key, 48 * 60 * 60); // 48시간 TTL (초 단위) (TimeToLive) 48시간이 지나면 redis가 알아서 삭제
            return null;
        });
        return written[0];
    }
}
