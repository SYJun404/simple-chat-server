package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.ChatMessage;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.ChatMessageRepository;
import com.syjun.chat.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final MessagePushService messagePushService;

    /**
     * 查询两个用户之间的聊天记录
     */
    public ApiResponse<List<ChatMessageResponse>> getChatHistory(Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findChatHistory(
            userId
        );

        List<ChatMessageResponse> list = messages
            .stream()
            .map(ChatMessageResponse::from)
            .toList();

        return ApiResponse.success(list);
    }

    /**
     * 发送聊天消息: 存入数据库 → 通过 WebSocket + TCP 双通道推送给接收者
     */
    public ApiResponse<ChatMessageResponse> sendMessage(
        SendMessageRequest request
    ) {
        // 1. 存入数据库
        ChatMessage msg = ChatMessage.builder()
            .fromUserId(request.getFromUserId())
            .toUserId(request.getToUserId())
            .content(request.getContent())
            .isRead(0)
            .build();

        msg = chatMessageRepository.save(msg);

        ChatMessageResponse response = ChatMessageResponse.from(msg);
        User fromUser = userRepository
            .findById(request.getFromUserId())
            .orElse(null);
        User toUser = userRepository
            .findById(request.getToUserId())
            .orElse(null);

        // 2. 双通道推送 (WebSocket + TCP)
        if (toUser != null) {
            messagePushService.sendChatMessage(
                fromUser.getUsername(),
                toUser.getUsername(),
                response,
                true
            );
        }

        return ApiResponse.success("发送成功", response);
    }

    public ApiResponse<ChatMessageResponse> sendSwingMessage(
        SendMessageRequest request,
        int platformType
    ) {
        // 1. 存入数据库
        ChatMessage msg = ChatMessage.builder()
            .fromUserId(request.getFromUserId())
            .toUserId(request.getToUserId())
            .content(request.getContent())
            .isRead(0)
            .build();

        msg = chatMessageRepository.save(msg);

        // 2. 双通道推送 + 跨平台推送
        ChatMessageResponse response = ChatMessageResponse.from(msg);
        User toUser = userRepository
            .findById(request.getToUserId())
            .orElse(null);

        User fromUser = userRepository
            .findById(request.getFromUserId())
            .orElse(null);

        if (toUser != null && fromUser != null) {
            messagePushService.sendChatMessageWithSelf(
                toUser.getUsername(),
                fromUser.getUsername(),
                response,
                platformType
            );
        }

        return ApiResponse.success("发送成功", response);
    }
}
