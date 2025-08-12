package com.minjeok4go.petplace.pet.service;

import com.minjeok4go.petplace.pet.dto.CreatePetRequest;
import com.minjeok4go.petplace.pet.dto.CreatePetResponse;
import com.minjeok4go.petplace.pet.dto.PetResponse;
import com.minjeok4go.petplace.pet.dto.SetDefaultPetRequest;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.pet.repository.PetRepository;
import com.minjeok4go.petplace.profile.dto.MyProfileResponse;
import com.minjeok4go.petplace.profile.service.ProfileService;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserService userService;
    private final ProfileService profileService;

    @Transactional
    public CreatePetResponse createPet(CreatePetRequest req, User user) {
        Pet pet = new Pet(req, user);

        Pet saved = petRepository.save(pet);
        return new CreatePetResponse(saved);
    }

    @Transactional(readOnly = true)
    public PetResponse getPet(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("pet Not Found id: " + id));
        return new PetResponse(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponse> getMyPets(User user) {
        return petRepository.findByUserId(user.getId())
                .stream().map(PetResponse::new).toList();
    }

    @Transactional
    public CreatePetResponse updatePet(Long id, CreatePetRequest req, User user) {
        Pet pet = petRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("pet Not Found or not user's pet petId: " + id + " userId: " + user.getId()));

        pet.setPet(req);

        Pet saved = petRepository.save(pet);

        return new CreatePetResponse(saved);
    }

    @Transactional
    public CreatePetResponse deletePet(Long id, User user) {
        Pet pet = petRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("pet Not Found or not user's pet petId: " + id + " userId: " + user.getId()));

        petRepository.delete(pet);

        return new CreatePetResponse(pet);
    }

    @Transactional
    public MyProfileResponse setDefaultPet(SetDefaultPetRequest req, User user) {
        Pet pet = petRepository.findByIdAndUserId(req.getId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("pet Not Found or not user's pet petId: " + req.getId() + " userId: " + user.getId()));

        user.setDefaultPetId(pet);

        userService.updateDefaultPet(user);

        return profileService.getProfile(user.getId());
    }
}
