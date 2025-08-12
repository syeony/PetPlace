package com.minjeok4go.petplace.feed.dto;

import com.minjeok4go.petplace.common.constant.FeedCategory;
import java.time.LocalDateTime;

public interface PopularFeedProjection {
    Long getId();
    String getContent();
    Long getUserId();
    String getUserNick();
    String getUserImg();
    Long getRegionId();
    FeedCategory getCategory();
    LocalDateTime getCreatedAt();
    Integer getLikes();
    Integer getViews();

    // Pet 쪽
    Long getPetId();
    String getPetName();
    String getPetAnimal();
    String getPetBreed();
}

