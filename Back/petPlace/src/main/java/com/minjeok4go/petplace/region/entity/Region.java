package com.minjeok4go.petplace.region.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "regions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Region {

    @Id
    private Long id;

    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    // geometry 컬럼은 생략(필요 시 Spatial 타입 적용 가능)
    // private Point geometry;
}
