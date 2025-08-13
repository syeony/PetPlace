package com.minjeok4go.petplace.pet.dto;

import com.minjeok4go.petplace.common.constant.Animal;
import com.minjeok4go.petplace.common.constant.Breed;
import com.minjeok4go.petplace.pet.entity.Pet;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PetResponse {

    private Long id;

    private String name;

    private String animal;

    private String breed;

    private Pet.Sex sex;

    private LocalDate birthday;

    private String imgSrc;

    private boolean tnr;

    public PetResponse(Pet pet) {
        this.id = pet.getId();
        this.name = pet.getName();
        this.animal = pet.getAnimal().getDisplayName();
        this.breed = pet.getBreed().getDisplayName();
        this.sex = pet.getSex();
        this.birthday = pet.getBirthday();
        this.imgSrc = pet.getImgSrc();
        this.tnr = pet.isTnr();
    }
}
