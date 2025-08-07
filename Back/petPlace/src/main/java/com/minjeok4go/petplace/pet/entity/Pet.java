package com.minjeok4go.petplace.pet.entity;

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

    public enum Sex {
        MALE,
        FEMALE
    }
}
