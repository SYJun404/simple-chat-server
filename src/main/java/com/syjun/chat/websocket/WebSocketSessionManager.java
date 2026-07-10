package com.syjun.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.syjun.chat.repository.UserRepository;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 会话管理器
 * 维护 username → WebSocketSession 的映射，实现点对点推送
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    private final UserRepository userRepository;

    /** username → WebSocketSession */
    private final Map<String, WebSocketSession> sessionMap =
        new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 禁止把 `LocalDateTime` 序列化成数组

    @Autowired
    public WebSocketSessionManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 注册会话。同一个用户只保留最新连接，旧连接会被关闭。
     */
    public void register(String username, WebSocketSession session) {
        // 关闭旧连接，保证一个用户永远只有一个活着的连接
        WebSocketSession oldSession = sessionMap.put(username, session);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close();
                log.info("用户 {} 旧连接已关闭，替换为新连接", username);
            } catch (IOException e) {
                log.warn(
                    "关闭用户 {} 旧连接失败: {}",
                    username,
                    e.getMessage()
                );
            }
        }
        log.info("用户 {} 上线，当前在线: {}", username, sessionMap.size());
    }

    /** 移除会话（仅当 Map 中存的确实是这个 session 时才移除） */
    public void remove(String username, WebSocketSession session) {
        // 用户下线，将status设置为0
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setStatus(0);
            userRepository.save(user);
        });

        sessionMap.remove(username, session);
        log.info("用户 {} 下线，当前在线: {}", username, sessionMap.size());
    }

    /** 判断用户是否在线 */
    public boolean isOnline(String username) {
        return sessionMap.containsKey(username);
    }

    /** 获取在线用户数 */
    public int getOnlineCount() {
        return sessionMap.size();
    }

    /**
     * 向指定用户发送消息
     */
    public void sendToUser(String toUsername, Object message) {
        WebSocketSession session = sessionMap.get(toUsername);
        if (session == null || !session.isOpen()) {
            log.warn("用户 {} 不在线，消息发送失败", toUsername);
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("向用户 {} 发送消息失败: {}", toUsername, e.getMessage());
        }
    }

    /**
     * 向所有在线用户广播消息
     */
    public void broadcast(Object message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            log.error("广播消息序列化失败: {}", e.getMessage());
            return;
        }

        sessionMap.forEach((username, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.error(
                        "向用户 {} 广播失败: {}",
                        username,
                        e.getMessage()
                    );
                }
            }
        });
        log.info("消息已广播给 {} 个在线用户", sessionMap.size());
    }
}
