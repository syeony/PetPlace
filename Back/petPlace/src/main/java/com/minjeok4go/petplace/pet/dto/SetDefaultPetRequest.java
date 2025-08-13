package com.minjeok4go.petplace.pet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SetDefaultPetRequest {

    @NotNull
    private Long id;
}
