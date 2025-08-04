package com.minjeok4go.petplace.image.controller;

import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Validated
public class ImageController {
    private final ImageService imageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse upload(@Valid @RequestBody ImageRequest req) {
        return imageService.upload(req);
    }

    @GetMapping
    public List<ImageResponse> list(@RequestParam ImageType refType, @RequestParam Long refId) {
        return imageService.getImages(refType, refId);
    }

}
