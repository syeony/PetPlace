package com.minjeok4go.petplace.region.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "regions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Region {

    @Id
    private Long id;

    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    // geometry 컬럼 - MySQL GEOMETRY 타입, NULL 허용
    @Column(columnDefinition = "POINT", nullable = true)
    private Point geometry;
}
