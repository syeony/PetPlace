package com.minjeok4go.petplace.profile.dto;

import com.minjeok4go.petplace.profile.entity.Introduction;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateIntroductionResponse {

    private Long id;

    private Long user_id;

    private String user_name;

    private String content;

    public CreateIntroductionResponse(Introduction introduction) {
        this.id = introduction.getId();
        this.user_id = introduction.getUser().getId();
        this.user_name = introduction.getUser().getUserName();
        this.content = introduction.getContent();
    }
}
