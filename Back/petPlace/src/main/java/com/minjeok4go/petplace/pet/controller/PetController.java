package com.minjeok4go.petplace.pet.controller;

import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.pet.dto.CreatePetRequest;
import com.minjeok4go.petplace.pet.dto.CreatePetResponse;
import com.minjeok4go.petplace.pet.dto.PetResponse;
import com.minjeok4go.petplace.pet.dto.SetDefaultPetRequest;
import com.minjeok4go.petplace.pet.service.PetService;
import com.minjeok4go.petplace.profile.dto.MyProfileResponse;
import com.minjeok4go.petplace.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pet API", description = "펫 API")
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PetController {

    private final PetService petService;
    private final AuthService authService;

    @Operation(
            summary = "펫 조회",
            description = "Path로 넘어온 ID의 펫을 조회합니다."
    )
    @GetMapping("/{id}")
    public PetResponse getPet(@PathVariable Long id){
        return petService.getPet(id);
    }

    @Operation(
            summary = "내 펫 조회",
            description = "토큰으로 넘어온 유저 ID의 펫들을 조회합니다."
    )
    @GetMapping("/me")
    public List<PetResponse> getMyPets(@AuthenticationPrincipal String tokenUserId){
        User me = authService.getUserFromToken(tokenUserId);
        return petService.getMyPets(me);
    }

    @Operation(
            summary = "내 펫 추가",
            description = "토큰으로 넘어온 유저 ID의 펫을 추가합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePetResponse createPet(@Valid @RequestBody CreatePetRequest req,
                                       @AuthenticationPrincipal String tokenUserId){
        User me = authService.getUserFromToken(tokenUserId);
        return petService.createPet(req, me);
    }

    @Operation(
            summary = "내 펫 수정",
            description = "Path 변수로 받아온 ID의 펫을 수정합니다."
    )
    @PutMapping("/{id}")
    public CreatePetResponse updatePet(@PathVariable Long id,
                                       @Valid @RequestBody CreatePetRequest req,
                                       @AuthenticationPrincipal String tokenUserId){
        User me = authService.getUserFromToken(tokenUserId);
        return petService.updatePet(id, req, me);
    }

    @Operation(
            summary = "내 펫 삭제",
            description = "Path 변수로 받아온 ID의 펫을 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public CreatePetResponse deletePet(@PathVariable Long id,
                                       @AuthenticationPrincipal String tokenUserId){
        User me = authService.getUserFromToken(tokenUserId);
        return petService.deletePet(id, me);
    }

    @Operation(
            summary = "대표 펫 등록",
            description = "토큰으로 넘어온 유저 ID의 대표 펫을 설정합니다."
    )
    @PostMapping("/set_default_pet")
    public MyProfileResponse setDefaultPet(@Valid @RequestBody SetDefaultPetRequest req,
                                           @AuthenticationPrincipal String tokenUserId){
        User me = authService.getUserFromToken(tokenUserId);
        return petService.setDefaultPet(req, me);
    }
}
