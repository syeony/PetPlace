package com.minjeok4go.petplace.profile.dto;

import com.minjeok4go.petplace.profile.entity.Introduction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteIntroductionResponse {
    private Long id;

    public DeleteIntroductionResponse(Introduction introduction){
        this.id = introduction.getId();
    }
}
