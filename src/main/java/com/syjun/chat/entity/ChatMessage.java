package com.syjun.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发送者ID */
    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    /** 接收者ID */
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    /** 消息内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 发送时间 */
    @Column(name = "send_time")
    @Builder.Default
    private LocalDateTime sendTime = LocalDateTime.now();

    /** 是否已读: 0-未读, 1-已读 */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Integer isRead = 0;
}
