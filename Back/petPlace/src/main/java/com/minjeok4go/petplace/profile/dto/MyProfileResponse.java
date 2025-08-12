package com.minjeok4go.petplace.profile.dto;


import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.pet.dto.PetResponse;
import com.minjeok4go.petplace.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Getter
@NoArgsConstructor
public class MyProfileResponse {

    private Long userId;

    private String nickname;

    private String regionName;

    private Integer defaultPetId;

    private String userImgSrc;

    private BigDecimal petSmell;

    private Integer defaultBadgeId;

    private Integer level;

    private Integer experience;

    private String introduction;

    private List<PetResponse> petList;

    private List<ImageResponse> imgList;

    public MyProfileResponse(User user, String regionName,
                             List<PetResponse> petList, List<ImageResponse> imgList){
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.regionName = regionName;
        this.defaultPetId = user.getDefaultPetId();
        this.userImgSrc = user.getUserImgSrc();
        this.petSmell = user.getPetSmell();
        this.defaultBadgeId = user.getDefaultBadgeId();
        this.level = user.getLevel();
        this.experience = user.getExperience();
        this.introduction = user.getIntroduction() != null ? user.getIntroduction().getContent() : null;
        this.petList = petList;
        this.imgList = imgList;
    }
}
