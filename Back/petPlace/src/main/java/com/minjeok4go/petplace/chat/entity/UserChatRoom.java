package com.minjeok4go.petplace.chat.entity;

import com.minjeok4go.petplace.chat.entity.ChatRoom;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_chat_rooms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // chat_room_id (DB) <-> chatRoom (Java)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false) // 컬럼명 맞추기!
    private ChatRoom chatRoom;

    // user_id (DB) <-> user (Java)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 컬럼명 맞추기!
    private User user;

    // last_read_cid (DB) <-> lastReadCid (Java)
    @Column(name = "last_read_cid")
    private Long lastReadCid;

    // leave_at (DB) <-> leaveAt (Java)
    @Column(name = "leave_at")
    private LocalDateTime leaveAt;
}
