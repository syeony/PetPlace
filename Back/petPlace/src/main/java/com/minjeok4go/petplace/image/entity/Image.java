package com.minjeok4go.petplace.image.entity;

import com.minjeok4go.petplace.common.constant.RefType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "ref_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RefType refType;

    @Column(name = "src", nullable = false)
    private String src;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    public Image(Long refId, RefType refType, String src, Integer sort) {
        this.refId = refId;
        this.refType = refType;
        this.src = src;
        this.sort = sort;
    }

    public void changeSrc(String src){
        this.src = src;
    }
}
