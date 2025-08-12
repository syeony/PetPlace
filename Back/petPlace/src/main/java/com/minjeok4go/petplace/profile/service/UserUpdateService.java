package com.minjeok4go.petplace.profile.service;

import com.minjeok4go.petplace.image.dto.UserProfileImageRequest;
import com.minjeok4go.petplace.profile.dto.MyProfileResponse;
import com.minjeok4go.petplace.profile.dto.UpdateUserRequest;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserUpdateService {

    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
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

    @Transactional
    public MyProfileResponse updateUser(UpdateUserRequest req, User user) {

        String newRaw = req.getNewPassword();
        String encodedPw = null;

        if (newRaw != null && !newRaw.isBlank()) {
            String curRaw = req.getCurPassword();

            if (curRaw == null || !passwordEncoder.matches(curRaw, user.getPassword())) {
                throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
            }

            if (passwordEncoder.matches(newRaw, user.getPassword())) {
                throw new IllegalArgumentException("새 비밀번호가 현재 비밀번호와 같습니다.");
            }
            encodedPw = passwordEncoder.encode(newRaw);
        }

        user.update(req, encodedPw);

        User updated = userService.updateUser(user);

        return profileService.getProfile(updated.getId());
    }
}
