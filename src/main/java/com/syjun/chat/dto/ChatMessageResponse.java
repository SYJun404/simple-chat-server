package com.syjun.chat.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public String toJsonStr() {
        try {
            return JSON_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
