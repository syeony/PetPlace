package com.minjeok4go.petplace.image.service;

import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import jakarta.persistence.EntityNotFoundException;
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
    @Transactional(readOnly = true)
    public List<ImageResponse> getImages(RefType refType, Long refId) {
        return imageRepository
                .findByRefTypeAndRefIdOrderBySortAsc(refType, refId)
                .stream()
                .map(img -> new ImageResponse(img.getId(), img.getSrc(), img.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ImageResponse createImages(ImageRequest req) {
        Image image = new Image(req.getRefId(), req.getRefType(), req.getSrc(), req.getSort());

        image = imageRepository.save(image);
        return new ImageResponse(image.getId(), image.getSrc(), image.getSort());
    }
    @Override
    @Transactional
    public ImageResponse deleteImages(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id " + id));

        imageRepository.delete(image);
        return new ImageResponse(image.getId(), image.getSrc(), image.getSort());
    }
}
