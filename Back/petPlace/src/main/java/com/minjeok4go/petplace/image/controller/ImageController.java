package com.minjeok4go.petplace.image.controller;

import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.service.ImageService;
import com.minjeok4go.petplace.profile.dto.CreateIntroductionRequest;
import com.minjeok4go.petplace.profile.dto.CreateIntroductionResponse;
import com.minjeok4go.petplace.profile.dto.DeleteIntroductionResponse;
import com.minjeok4go.petplace.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Image API", description = "이미지 API")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ImageController {

    private final ImageService imageService;
    private final AuthService authService;

    @Operation(
            summary = "이미지 등록",
            description = "각종 테이블에서 사용하는 이미지를 등록합니다.\n" +
                    "Image Upload API를 사용한 후 반환된 src를 활용합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse createMyProfile(@Valid @RequestBody ImageRequest req,
                                         @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return imageService.createImages(req);
    }

    @Operation(
            summary = "이미지 삭제",
            description = "등록된 이미지를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ImageResponse deleteMyProfile(@PathVariable Long id,
                                                      @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return imageService.deleteImages(id);
    }
}
