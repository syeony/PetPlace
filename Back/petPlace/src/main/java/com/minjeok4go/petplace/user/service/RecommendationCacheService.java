package com.minjeok4go.petplace.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendationCacheService {

    private final StringRedisTemplate redis;

    private static final String GROUP_KEY_PREFIX = "group:";          // ZSET: group:<groupKey>
    private static final String FEED_GROUPS_KEY_PREFIX = "feed_groups:"; // SET: feed_groups:<feedId> -> groupKey 모음

    /** 배치에서 ZADD할 때 함께 호출해서, feed가 어떤 그룹에 들어갔는지 인덱싱 */
    public void rememberMembership(Long feedId, String groupKey) {
        redis.opsForSet().add(FEED_GROUPS_KEY_PREFIX + feedId, groupKey);
    }

    /** 특정 feed가 수정/삭제되면 모든 소속 그룹 캐시에서 제거 */
    public void evictByFeedId(Long feedId) {
        String idxKey = FEED_GROUPS_KEY_PREFIX + feedId;
        Set<String> groups = redis.opsForSet().members(idxKey);
        if (groups != null) {
            for (String g : groups) {
                redis.opsForZSet().remove(GROUP_KEY_PREFIX + g, String.valueOf(feedId));
            }
        }
        redis.delete(idxKey);
    }
}
