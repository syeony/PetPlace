package com.minjeok4go.petplace.image.service;

import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    @Override
    @Transactional
    public ImageResponse upload(ImageRequest req) {
        Image image = new Image(req.getRefId(), req.getRefType(), req.getSrc(), req.getSort());

        image = imageRepository.save(image);
        return new ImageResponse(image.getSrc(), image.getSort());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getImages(ImageType refType, Long refId) {
        return imageRepository
                .findByRefTypeAndRefIdOrdOrderBySortAsc(refType, refId)
                .stream()
                .map(img -> new ImageResponse(img.getSrc(), img.getSort()))
                .collect(Collectors.toList());
    }
}
