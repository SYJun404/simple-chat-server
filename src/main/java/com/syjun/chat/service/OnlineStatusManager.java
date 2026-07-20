package com.syjun.chat.service;

import com.syjun.chat.customTcp.TcpServer;
import com.syjun.chat.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 协调 WS 和 TCP 两端的在线状态判断与广播，
 * 避免 WebSocketSessionManager 和 TcpServer 互相依赖。
 */
@Slf4j
@Component
public class OnlineStatusManager {

    private final WebSocketSessionManager sessionManager;
    private final TcpServer tcpServer;

    public OnlineStatusManager(
        @Lazy WebSocketSessionManager sessionManager,
        @Lazy TcpServer tcpServer
    ) {
        this.sessionManager = sessionManager;
        this.tcpServer = tcpServer;
    }

    /** 用户是否在任意端在线 */
    public boolean isUserOnline(String username) {
        return sessionManager.isOnline(username) || isTcpOnline(username);
    }

    /** 用户是否在 TCP 端在线 */
    public boolean isTcpOnline(String username) {
        return (
            tcpServer.isOnline(username) || tcpServer.isOnline(username + "@")
        );
    }

    /** 在 TCP 端广播用户上线 */
    public void broadcastTcpLogin(String username) {
        tcpServer.broadcast("LOGIN|" + username);
    }

    /** 在 TCP 端广播用户下线 */
    public void broadcastTcpLogout(String username) {
        tcpServer.broadcast("LOGOUT|" + username);
    }

    /** 在 WebSocket 端广播用户上线 */
    public void broadcastWsLogin(Long userId) {
        sessionManager.broadcast(
            com.syjun.chat.dto.WsMessage.builder()
                .type("friend_accepted")
                .data(userId)
                .build()
        );
    }

    /** 在 WebSocket 端广播用户下线 */
    public void broadcastWsLogout(Long userId) {
        sessionManager.broadcast(
            com.syjun.chat.dto.WsMessage.builder()
                .type("friend_accepted")
                .data(userId)
                .build()
        );
    }
}
