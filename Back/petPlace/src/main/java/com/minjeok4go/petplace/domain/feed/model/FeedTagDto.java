package com.minjeok4go.petplace.domain.feed.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedTagDto {
    private Long feedId;  // ✅ fid → feedId
    private Long tagId;   // ✅ tid → tagId
}
