package com.minjeok4go.petplace.profile.service;

import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.image.dto.FeedImageRequest;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.entity.Image;
import com.minjeok4go.petplace.image.repository.ImageRepository;
import com.minjeok4go.petplace.pet.dto.PetResponse;
import com.minjeok4go.petplace.pet.repository.PetRepository;
import com.minjeok4go.petplace.profile.dto.CreateIntroductionRequest;
import com.minjeok4go.petplace.profile.dto.CreateIntroductionResponse;
import com.minjeok4go.petplace.profile.dto.DeleteIntroductionResponse;
import com.minjeok4go.petplace.profile.dto.MyProfileResponse;
import com.minjeok4go.petplace.profile.entity.Introduction;
import com.minjeok4go.petplace.profile.repository.IntroductionRepository;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.region.repository.RegionRepository;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserService userService;
    private final RegionRepository regionRepository;
    private final ImageRepository imageRepository;
    private final PetRepository petRepository;
    private final IntroductionRepository introductionRepository;

    @Transactional(readOnly = true)
    public MyProfileResponse getProfile(Long id) {

        User user = userService.getUserById(id);

        Region region = regionRepository.findById(user.getRegionId())
                .orElseThrow(() -> new EntityNotFoundException("Region not found with id " + user.getRegionId()));

        List<PetResponse> petList = petRepository
                .findByUserId(id).stream()
                .map(PetResponse::new).toList();

        List<ImageResponse> imgList = imageRepository
                .findByRefTypeAndRefIdOrderBySortAsc(RefType.USER, user.getId())
                .stream()
                .map(img -> new ImageResponse(img.getId(), img.getSrc(), img.getSort()))
                .toList();

        return new MyProfileResponse(user, region.getName(), petList, imgList);
    }

    @Transactional
    public CreateIntroductionResponse createIntroduction(CreateIntroductionRequest req, User user){
        Introduction introduction = new Introduction(user, req.getContent());
        Introduction saved = introductionRepository.save(introduction);

        return new CreateIntroductionResponse(saved);
    }

    @Transactional
    public CreateIntroductionResponse updateIntroduction(CreateIntroductionRequest req, User user){
        Introduction introduction = introductionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Intro not found with id " + user.getId()));

        introduction.updateContent(req);

        Introduction saved = introductionRepository.save(introduction);

        return new CreateIntroductionResponse(saved);
    }

    @Transactional
    public DeleteIntroductionResponse deleteIntroduction(User user){
        Introduction introduction = introductionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Intro not found with id " + user.getId()));

        user.setIntroduction(null);
        introductionRepository.delete(introduction);

        return new DeleteIntroductionResponse(introduction);
    }

    @Transactional
    public ImageResponse createUserImage(FeedImageRequest req, User user) {
        Image image = new Image(user.getId(), RefType.USER, req.getSrc(), req.getSort());

        Image saved = imageRepository.save(image);

        return new ImageResponse(saved);
    }

    @Transactional
    public ImageResponse updateUserImage(FeedImageRequest req, User user) {
        Image image = imageRepository.findByRefTypeAndRefIdAndSort(RefType.USER, user.getId(), req.getSort())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Image not found with id " + user.getId() + ", sort " + req.getSort())
                );

        image.changeSrc(req.getSrc());

        Image saved = imageRepository.save(image);

        return new ImageResponse(saved);
    }

    @Transactional
    public ImageResponse deleteUserImage(Integer sort, User user) {
        Image image = imageRepository.findByRefTypeAndRefIdAndSort(RefType.USER, user.getId(), sort)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Image not found with id " + user.getId() + ", sort " + sort)
                );

        imageRepository.delete(image);

        return new ImageResponse(image);
    }
}
