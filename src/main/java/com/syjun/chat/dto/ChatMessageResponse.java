package com.syjun.chat.dto;

import com.syjun.chat.entity.ChatMessage;
import java.time.LocalDateTime;
import lombok.*;

/** 聊天消息响应 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private LocalDateTime sendTime;
    private Integer isRead;

    public static ChatMessageResponse from(ChatMessage msg) {
        return ChatMessageResponse.builder()
            .id(msg.getId())
            .fromUserId(msg.getFromUserId())
            .toUserId(msg.getToUserId())
            .content(msg.getContent())
            .sendTime(msg.getSendTime())
            .isRead(msg.getIsRead())
            .build();
    }
}
