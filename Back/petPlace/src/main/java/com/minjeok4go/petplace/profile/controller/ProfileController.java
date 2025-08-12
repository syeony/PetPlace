package com.minjeok4go.petplace.profile.controller;

import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.image.dto.FeedImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.dto.UserProfileImageRequest;
import com.minjeok4go.petplace.profile.dto.*;
import com.minjeok4go.petplace.profile.service.ProfileService;
import com.minjeok4go.petplace.profile.service.UserUpdateService;
import com.minjeok4go.petplace.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile API", description = "프로필 API")
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;
    private final UserUpdateService userUpdateService;
    private final AuthService authService;

    @Operation(
            summary = "다른 유저 프로필 조회",
            description = "path 변수로 넘어온 유저 ID에 해당하는 프로필을 반환합니다."
    )
    @GetMapping("/{id}")
    public MyProfileResponse getProfile(@PathVariable Long id) {
        return profileService.getProfile(id);
    }

    @Operation(
            summary = "내 프로필 조회",
            description = "토큰으로 넘어온 유저 ID에 해당하는 프로필을 반환합니다."
    )
    @GetMapping("/me")
    public MyProfileResponse getMyProfile(@AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return profileService.getProfile(me.getId());
    }

    @Operation(
            summary = "내 정보 수정",
            description = "토큰으로 넘어온 유저 ID에 해당하는 유저의\n" +
                    "닉네임, 비밀번호, 프로필 이미지, 지역정보를 수정합니다."
    )
    @PutMapping("/me/update")
    public MyProfileResponse updateMyInformation(@Valid @RequestBody UpdateUserRequest req,
                                                  @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return userUpdateService.updateUser(req, me);
    }

    @Operation(
            summary = "내 프로필 이미지 추가/수정",
            description = "토큰으로 넘어온 유저 ID에 해당하는 유저의\n" +
                    "프로필 이미지를 추가, 수정합니다."
    )
    @PutMapping("/me/profile_image")
    public MyProfileResponse updateMyProfileImage(@Valid @RequestBody UserProfileImageRequest imgSrc,
                                                  @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return userUpdateService.updateUserImage(imgSrc, me);
    }

    @Operation(
            summary = "내 프로필 이미지 삭제",
            description = "토큰으로 넘어온 유저 ID에 해당하는 유저의\n" +
                    "프로필 이미지를 삭제합니다."
    )
    @DeleteMapping("/me/profile_image")
    public MyProfileResponse deleteMyProfileImage(@AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return userUpdateService.deleteUserImage(me);
    }

    @Operation(
            summary = "내 프로필 소개글 등록",
            description = "토큰으로 넘어온 유저 ID의 소개글을 등록합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateIntroductionResponse createMyProfile(@Valid @RequestBody CreateIntroductionRequest req,
                                                      @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return profileService.createIntroduction(req, me);
    }

    @Operation(
            summary = "내 프로필 소개글 수정",
            description = "토큰으로 넘어온 유저 ID의 소개글을 수정합니다."
    )
    @PutMapping("/me")
    public CreateIntroductionResponse updateMyProfile(@Valid @RequestBody CreateIntroductionRequest req,
                                                      @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return profileService.updateIntroduction(req, me);
    }

    @Operation(
            summary = "내 프로필 소개글 삭제",
            description = "토큰으로 넘어온 유저 ID의 소개글을 삭제합니다."
    )
    @DeleteMapping("/me")
    public DeleteIntroductionResponse deleteMyProfile(@AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return profileService.deleteIntroduction(me);
    }

    @Operation(
            summary = "내 펫 용품 이미지 등록",
            description = "토큰으로 넘어온 유저 ID의 펫 용품 이미지를 등록합니다.\n" +
                    "sort에 1, 2, 3까지만 입력해주세요."
    )
    @PostMapping("/me/user_image")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse createMyImage(@Valid @RequestBody FeedImageRequest req,
                                       @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return profileService.createUserImage(req, me);
    }

    @Operation(
            summary = "내 펫 용품 이미지 수정",
            description = "토큰으로 넘어온 유저 ID의 펫 용품 이미지를 수정합니다.\n" +
                    "sort에 1, 2, 3까지만 입력해주세요."
    )
    @PutMapping("/me/user_image")
    public ImageResponse updateMyImage(@Valid @RequestBody FeedImageRequest req,
                                       @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return profileService.updateUserImage(req, me);
    }

    @Operation(
            summary = "내 펫 용품 이미지 삭제",
            description = "토큰으로 넘어온 유저 ID와 Path 변수로 넘어온 sort로\n" +
                    "펫 용품 이미지를 삭제합니다."
    )
    @DeleteMapping("/me/user_image/{sort}")
    public ImageResponse deleteMyProfile(@PathVariable Integer sort,
                                         @AuthenticationPrincipal String tokenUserId) {
        User me = authService.getUserFromToken(tokenUserId);
        return profileService.deleteUserImage(sort, me);
    }
}
