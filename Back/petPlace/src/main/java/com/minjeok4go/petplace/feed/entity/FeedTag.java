package com.minjeok4go.petplace.feed.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feed_tags")
@Getter
@NoArgsConstructor
public class FeedTag {

    @EmbeddedId
    private FeedTagId id = new FeedTagId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("feedId")  // FeedTagId의 feedId와 매핑
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")  // FeedTagId의 tagId와 매핑
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public FeedTag(Feed feed, Tag tag) {
        this.feed = feed;
        this.tag = tag;
        this.id = new FeedTagId(feed.getId(), tag.getId());
    }
}
