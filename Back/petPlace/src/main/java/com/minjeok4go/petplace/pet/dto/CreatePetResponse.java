package com.minjeok4go.petplace.pet.dto;

import com.minjeok4go.petplace.pet.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePetResponse {

    private Long id;

    private String name;

    private Integer age;

    private String imgSrc;

    public CreatePetResponse(Pet pet) {
        this.id = pet.getId();
        this.name = pet.getName();
        this.age = manAge(pet.getBirthday());
        this.imgSrc = pet.getImgSrc();
    }

    private int manAge(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            throw new IllegalArgumentException("생일이 오늘 이후입니다.");
        }
        return Period.between(birthDate, today).getYears();
    }
}
