package com.syjun.chat.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 聊天 WebSocket 处理器
 * 客户端连接时携带 userId 参数: ws://host/ws/chat?userId=1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = getUsername(session);
        if (username == null) {
            log.warn("WebSocket 连接缺少 userId 参数，关闭连接");
            try {
                session.close();
            } catch (Exception ignored) {}
            return;
        }

        sessionManager.register(username, session);
    }

    @Override
    protected void handleTextMessage(
        WebSocketSession session,
        TextMessage message
    ) {
        // 客户端发来的消息可以在这里处理（目前由 REST 接口接收）
        log.debug("收到 WebSocket 消息: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(
        WebSocketSession session,
        CloseStatus status
    ) {
        String username = getUsername(session);
        if (username != null) {
            sessionManager.remove(username, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        log.error("WebSocket 传输错误: {}", ex.getMessage());
        String username = getUsername(session);
        if (username != null) {
            sessionManager.remove(username, session);
        }
    }

    /** 从 URL 查询参数中提取 username */
    private String getUsername(WebSocketSession session) {
        String query =
            session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "username".equals(kv[0])) {
                try {
                    return kv[1];
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
