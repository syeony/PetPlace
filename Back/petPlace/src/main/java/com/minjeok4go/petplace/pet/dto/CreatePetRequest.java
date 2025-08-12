package com.minjeok4go.petplace.pet.dto;

import com.minjeok4go.petplace.common.constant.Animal;
import com.minjeok4go.petplace.common.constant.Breed;
import com.minjeok4go.petplace.pet.entity.Pet;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreatePetRequest {

    @NotNull
    private String name;

    @NotNull
    private Animal animal;

    @NotNull
    private Breed breed;

    @NotNull
    private Pet.Sex sex;

    @NotNull
    private LocalDate birthday;

    private String imgSrc;

    @NotNull
    private boolean tnr;
}
