package com.syjun.chat.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * 通过 WebSocket 推送给客户端的消息统一格式
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WsMessage<T> {

    /** 消息类型: chat / friend_request */
    private String type;

    /** 消息体 */
    private T data;

    /** 服务端时间戳 */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ---- 工厂方法 ----

    /** 聊天消息 */
    public static WsMessage<ChatMessageResponse> chat(ChatMessageResponse msg) {
        return WsMessage.<ChatMessageResponse>builder()
            .type("chat")
            .data(msg)
            .build();
    }

    /** 好友请求消息 */
    public static WsMessage<FriendRequestVO> friendRequest(
        FriendRequestVO request
    ) {
        return WsMessage.<FriendRequestVO>builder()
            .type("friend_request")
            .data(request)
            .build();
    }

    /** 好友请求被接受的通知，携带新好友ID */
    public static WsMessage<Long> friendAccepted(Long newFriendId) {
        return WsMessage.<Long>builder()
            .type("friend_accepted")
            .data(newFriendId)
            .build();
    }
}
