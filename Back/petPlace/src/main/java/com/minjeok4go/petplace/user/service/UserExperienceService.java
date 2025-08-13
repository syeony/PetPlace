package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.common.constant.ActivityType;
import com.minjeok4go.petplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserExperienceService {
    private final UserPetSmell userPetSmell = new UserPetSmell();

    @Transactional
    public void applyActivity(User user, ActivityType activity) {
        user.applyExpDelta(activity.getExpDelta(), userPetSmell);
    }
}
