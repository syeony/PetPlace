package com.minjeok4go.petplace.domain.feed.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Feed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private Long uid;             // 작성자 유저 인덱스

    @JsonProperty("user_nick")
    private String userNick;      // 작성자 닉네임 (ERD 기준 user_nick)

    @JsonProperty("user_img")
    private String userImg;       // 작성자 프로필 이미지

    private Long rid;          // 지역 인덱스

    @Enumerated(EnumType.STRING)
    private FeedCategory category;      // ENUM 값 ("0", "1", "2", ...)

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "`like`")
    private Integer like;

    private Integer view;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<Hashtag> hashtags;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<Comment> comments;


//    public void increaseLike() {
//        like = like+1;
//    }
//
//    public void decreaseLike() {
//        like = like-1;
//    }
}
