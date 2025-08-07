package com.minjeok4go.petplace.feed.entity;

import com.minjeok4go.petplace.comment.entity.Comment;
import com.minjeok4go.petplace.common.constant.FeedCategory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@EntityListeners(AuditingEntityListener.class)
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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

    @Builder.Default
    @Column(nullable = false)
    private Integer likes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer views = 0;

    private int likeCount;

    public int getLikeCount() {
        return likeCount;
    }
    // 실시간 comment count
    public int getCommentCount() {
        return comments == null ? 0 : comments.size();
    }
    // DB에 commentCOunt칼럼이 있다면
//    private int commentCount;
//
//    public int getCommentCount() {
//        return commentCount;
//    }
    private boolean dogRelated;

    public boolean isDogRelated() {
        return dogRelated;
    }
    // FeedCatgory가 있다면
//    public boolean isDogRelated() {
//        return this.category == FeedCategory.DOG;
//    }

    @Builder.Default
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<FeedTag> feedTags = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<Comment> comments = new HashSet<>();

    public Feed(Long feedId) {
        this.id = feedId;
    }

    public void update() {
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseLikes() {
        this.likes+=1;
    }

    public void decreaseLikes() {
        this.likes-=1;
    }
}
