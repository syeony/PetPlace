package com.minjeok4go.petplace.domain.feed.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feed_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
