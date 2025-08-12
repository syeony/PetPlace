package com.minjeok4go.petplace.image.dto;

import com.minjeok4go.petplace.image.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {

    private Long id;

    private String src;

    private Integer sort;

    public ImageResponse(Image img) {
        this.id = img.getId();
        this.src = img.getSrc();
        this.sort = img.getSort();
    }
}
