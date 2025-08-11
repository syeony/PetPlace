package com.minjeok4go.petplace.profile.entity;

import com.minjeok4go.petplace.profile.dto.CreateIntroductionRequest;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "introduction",
        uniqueConstraints = @UniqueConstraint(name = "uq_intro_uid", columnNames = "user_id")
)
@Getter
@NoArgsConstructor
public class Introduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "content", length = 2000, nullable = false)
    private String content;

    public Introduction(User user, String content) {
        this.user = user;
        this.content = content;
    }

    public void updateContent(CreateIntroductionRequest req){
        this.content = req.getContent();
    }
}
