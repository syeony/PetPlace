package com.minjeok4go.petplace.pet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeletePetResponse {

    private Long id;

    private Long userId;

    private String name;
}
