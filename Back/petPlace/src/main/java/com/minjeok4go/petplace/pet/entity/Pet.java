package com.minjeok4go.petplace.pet.entity;

import com.minjeok4go.petplace.common.constant.Animal;
import com.minjeok4go.petplace.common.constant.Breed;
import com.minjeok4go.petplace.pet.dto.CreatePetRequest;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "pets",
        uniqueConstraints = @UniqueConstraint(name = "uq_pet_uid_name", columnNames = {"user_id", "name"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id가 Long 타입임에 주의!
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 20, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Animal animal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Breed breed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sex sex;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(name = "img_src", length = 500)
    private String imgSrc;

    @Column(nullable = false)
    private boolean tnr; // 0/1 대신 boolean 타입 사용 (MySQL TINYINT→boolean 매핑)

    public Pet(CreatePetRequest req, User user) {
        this.userId = user.getId();
        this.name = req.getName();
        this.animal = req.getAnimal();
        this.breed = req.getBreed();
        this.sex = req.getSex();
        this.birthday = req.getBirthday();
        this.imgSrc = req.getImgSrc();
        this.tnr = req.isTnr();
    }

    public enum Sex {
        MALE,
        FEMALE
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public void setPet(CreatePetRequest req) {
        this.name = req.getName();
        this.animal = req.getAnimal();
        this.breed = req.getBreed();
        this.sex = req.getSex();
        this.birthday = req.getBirthday();
        this.imgSrc = req.getImgSrc();
        this.tnr = req.isTnr();
    }
}

