package com.minjeok4go.petplace.profile.service;

import com.minjeok4go.petplace.image.dto.UserProfileImageRequest;
import com.minjeok4go.petplace.profile.dto.MyProfileResponse;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserUpdateService {

    private final ProfileService profileService;
    private final UserService userService;

    @Transactional
    public MyProfileResponse updateUserImage(UserProfileImageRequest imgSrc, User user) {

        user.setImage(imgSrc.getImgSrc());

        User updated = userService.updateImage(user);

        return profileService.getProfile(updated.getId());
    }

    @Transactional
    public MyProfileResponse deleteUserImage(User user) {

        user.setImage(null);

        User updated = userService.updateImage(user);

        return profileService.getProfile(updated.getId());
    }
}
