package com.syjun.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friend_request_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发送者ID */
    @Column(name = "from_user_id", nullable = false)
    private String fromUserId;

    /** 接收者ID */
    @Column(name = "to_user_id", nullable = false)
    private String toUserId;

    /** 是否已读: 0-未读, 1-已读 */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Integer isRead = 0;
}
