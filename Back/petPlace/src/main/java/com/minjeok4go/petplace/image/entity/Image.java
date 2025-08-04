package com.minjeok4go.petplace.image.entity;

import com.minjeok4go.petplace.common.constant.ImageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "ref_type", nullable = false)
    private ImageType refType;

    @Column(name = "src", nullable = false)
    private String imageSrc;

    @Column(name = "sort", nullable = false)
    private String ImageSort;
}
