package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.ChatMessage;
import com.syjun.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * 查询两个用户之间的聊天记录
     */
    public ApiResponse<List<ChatMessageResponse>> getChatHistory(Long user1, Long user2) {
        List<ChatMessage> messages = chatMessageRepository.findChatHistory(user1, user2);

        List<ChatMessageResponse> list = messages.stream()
                .map(ChatMessageResponse::from)
                .toList();

        return ApiResponse.success(list);
    }
}
