package com.minjeok4go.petplace.domain.feed.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minjeok4go.petplace.domain.feed.model.Feed;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Repository
public class FeedDummyRepository {

    private final List<Feed> feedList;

    public FeedDummyRepository() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // LocalDateTime 매핑 지원 / 없으면 LocalDateTime 등에서 에러날 수 있음

            // Dummy Json
            InputStream is = getClass().getResourceAsStream("/data/feed_dummy_100.json");
            feedList = mapper.readValue(is, new TypeReference<List<Feed>>() {});
        } catch (Exception e) {
            throw new RuntimeException("피드 더미 데이터 로딩 실패!", e);
        }
    }

    // 전체 피드 반환
    public List<Feed> findAll() {
        return feedList;
    }

    // 1) 카테고리별 피드 반환
    public List<Feed> findByCategory(String category) {
        return feedList.stream()
                .filter(feed -> Objects.equals(feed.getCategory(), category))
                .collect(Collectors.toList());
    }

    // 2) 특정 태그가 포함된 피드 리스트 반환
    public List<Feed> findByTag(Integer tagId) {
        return feedList.stream()
                .filter(feed -> feed.getTags() != null && feed.getTags().contains(tagId))
                .collect(Collectors.toList());
    }

    // 3) 특정 유저닉네임이 쓴 피드 리스트 반환
    public List<Feed> findByUserNick(String userNick) {
        return feedList.stream()
                .filter(feed -> Objects.equals(feed.getUserNick(), userNick))
                .collect(Collectors.toList());
    }
}