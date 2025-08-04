package com.minjeok4go.petplace.feed.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDto {
    private Long id;
    private String name;  // ✅ tagName → name
}
