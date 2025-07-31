package com.minjeok4go.petplace.domain.feed.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class HashtagId implements Serializable {
    private Long fid;
    private Long tid;
}
