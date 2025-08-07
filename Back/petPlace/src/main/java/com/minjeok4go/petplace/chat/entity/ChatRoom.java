package com.minjeok4go.petplace.chat.entity;

import com.minjeok4go.petplace.user.entity.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id_1", "user_id_2"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 / 실제로 접근할 때 까지  DB에서 조회하지 않고 대기함 (불필요한 쿼리/조인을 막아서 성능 최적화)
    @JoinColumn(name = "user_id_1")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_2")
    private User user2;

    @Column(name = "last_message", nullable = false, length = 1000)
    private String lastMessage = "";

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
}

