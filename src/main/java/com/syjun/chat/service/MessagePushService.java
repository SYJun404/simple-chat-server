package com.syjun.chat.service;

import com.syjun.chat.customTcp.TcpServer;
import com.syjun.chat.dto.ChatMessageResponse;
import com.syjun.chat.dto.FriendRequestVO;
import com.syjun.chat.dto.WsMessage;
import com.syjun.chat.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 同时向 WebSocket 端（浏览器）和 TCP 端（Swing/Android）推送消息，
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePushService {

    private final WebSocketSessionManager sessionManager;
    private final TcpServer tcpServer;

    /**
     * 推送聊天消息给指定用户（双通道：WebSocket + TCP）
     */
    public void sendChatMessage(
        String fromUsername,
        String toUsername,
        ChatMessageResponse message,
        boolean isWeb
    ) {
        // 推送给 WebSocket 客户端（浏览器）
        sessionManager.sendToUser(toUsername, WsMessage.chat(message));
        // 推送给 TCP 客户端（Swing / Android）
        tcpServer.sendToUser(toUsername, message);

        // 推送给同一账号，不同平台
        if (isWeb) {
            tcpServer.sendToUser(fromUsername, message);
        } else {
            sessionManager.sendToUser(fromUsername, WsMessage.chat(message));
        }
    }

    /**
     * 聊天消息 + 给自己的另一个平台也发一份
     */
    public void sendChatMessageWithSelf(
        String toUsername,
        String fromUsername,
        ChatMessageResponse message,
        int platformType
    ) {
        sendChatMessage(fromUsername, toUsername, message, false);
        tcpServer.sendToSelfDiffPlatform(fromUsername, message, platformType);
    }

    /**
     * 推送好友请求给指定 TCP 用户
     */
    public void sendFriendRequest(
        String fromNickname,
        String toUsername,
        FriendRequestVO requestVO
    ) {
        tcpServer.sendFriendRequest(fromNickname, toUsername);
        sessionManager.sendToUser(
            toUsername,
            WsMessage.friendRequest(requestVO)
        );
    }

    /**
     * 推送好友接受通知
     */
    public void sendFriendAccept(
        String fromUsername,
        String toUsername,
        Long toUserId,
        Long fromUserId
    ) {
        tcpServer.sendFriendAccept(fromUsername, toUsername);

        sessionManager.sendToUser(
            toUsername,
            WsMessage.friendAccepted(fromUserId)
        );
        sessionManager.sendToUser(
            fromUsername,
            WsMessage.friendAccepted(toUserId)
        );
    }

    /**
     * 广播给所有在线用户（双通道）
     */
    public void broadcast(Object message, String tcpMessage) {
        sessionManager.broadcast(message);
        tcpServer.broadcast(tcpMessage);
    }

    /**
     * 判断用户是否在 TCP 端在线
     */
    public boolean isTcpOnline(String username) {
        return (
            tcpServer.isOnline(username) || tcpServer.isOnline(username + "@")
        );
    }

    /**
     * 判断用户是否在 WebSocket 端在线
     */
    public boolean isWsOnline(String username) {
        return sessionManager.isOnline(username);
    }
}
