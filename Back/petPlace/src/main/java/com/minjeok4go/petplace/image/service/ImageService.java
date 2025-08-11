package com.minjeok4go.petplace.image.service;

import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;

import java.util.List;

public interface ImageService {
    ImageResponse createImages(ImageRequest req);

    List<ImageResponse> getImages(ImageType refType, Long refId);




    ImageResponse deleteImages(Long id);

}
