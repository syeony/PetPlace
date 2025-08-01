package com.minjeok4go.petplace.domain.feed.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "Tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_name", unique = true, nullable = false)
    private String tagName;
}

//@Table(name = "Tag", uniqueConstraints = {@UniqueConstraint(columnNames = {"id", "tag_name"})})