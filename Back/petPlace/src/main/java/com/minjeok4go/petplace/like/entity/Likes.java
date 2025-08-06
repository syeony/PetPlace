package com.minjeok4go.petplace.like.entity;

import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(name = "uq_uid_fid", columnNames = {"user_id","feed_id"})
)
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "liked_at", nullable = false, updatable = false)
    private LocalDateTime likedAt;

    public Likes(Feed feed, User user) {
        this.feed = feed;
        this.user = user;
    }

    public Likes liked() {
        this.likedAt = LocalDateTime.now();
        return this;
    }
}
