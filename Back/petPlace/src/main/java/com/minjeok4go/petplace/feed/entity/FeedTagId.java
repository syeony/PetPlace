package com.minjeok4go.petplace.feed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class FeedTagId implements Serializable {
    @Column(name = "feed_id")
    private Long feedId;

    @Column(name = "tag_id")
    private Long tagId;

    FeedTagId(Long feedId, Long tagId){
        this.feedId = feedId;
        this.tagId = tagId;
    }
}
