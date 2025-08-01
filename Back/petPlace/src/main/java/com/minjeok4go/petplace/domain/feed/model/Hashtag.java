package com.minjeok4go.petplace.domain.feed.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Hashtag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hashtag {
    @EmbeddedId
    private HashtagId id = new HashtagId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fid")  // HashtagId의 fid와 매핑
    @JoinColumn(name = "fid")
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tid")  // HashtagId의 tid와 매핑
    @JoinColumn(name = "tid")
    private Tag tag;
}