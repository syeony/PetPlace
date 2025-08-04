package com.minjeok4go.petplace.feed.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedTagId implements Serializable {
    @Column(name = "feed_id")
    private Long feedId;

    @Column(name = "tag_id")
    private Long tagId;
}
