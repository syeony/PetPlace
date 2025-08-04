package com.minjeok4go.petplace.feed.entity;

import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.common.constant.FeedCategory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "feeds")
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

    @Column(name = "user_id", nullable = false)
    private Long userId;  // users.id는 INT 타입

    @Column(name = "user_nick", nullable = false)
    private String userNick;

    @Column(name = "user_img")
    private String userImg;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedCategory category;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "likes", nullable = false)
    private Integer likes = 0;

    @Column(name = "views", nullable = false)
    private Integer views = 0;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<FeedTag> feedTags = new HashSet<>();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<Comment> comments = new HashSet<>();
}
