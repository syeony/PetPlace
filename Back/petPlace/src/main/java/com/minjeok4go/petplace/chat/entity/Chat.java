package com.minjeok4go.petplace.chat.entity;

import com.minjeok4go.petplace.image.entity.Image;
import jakarta.persistence.*;

import com.minjeok4go.petplace.user.entity.User;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chats") // 테이블명 맞춤!
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // chat_rooms_id (DB) <-> chatRoom (Java)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_rooms_id", nullable = false)
    private ChatRoom chatRoom;

    // user_id (DB) <-> user (Java)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message", nullable = false, length = 1001)
    private String message;

    // created_at (DB)
    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT NOW()")
    private LocalDateTime createdAt = LocalDateTime.now();

    // 이 채팅에 첨부된 이미지들 (ref_id, ref_type으로 찾음)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_id", referencedColumnName = "id") // images 테이블의 ref_id가 Chat의 id를 참조
    @Where(clause = "ref_type = 'CHAT'")
    private List<Image> images;

}
