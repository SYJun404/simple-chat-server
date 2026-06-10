package com.syjun.chat.controller;

import com.syjun.chat.dto.*;
import com.syjun.chat.service.ChatMessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    /**
     * 查询两个用户之间的聊天记录
     */
    @GetMapping("/history")
    public ApiResponse<List<ChatMessageResponse>> getChatHistory(
        @RequestParam Long userId,
        @RequestParam Long friendId
    ) {
        return chatMessageService.getChatHistory(userId, friendId);
    }

    /**
     * 发送聊天消息: 存库 + WebSocket 推送
     */
    @PostMapping("/send")
    public ApiResponse<ChatMessageResponse> sendMessage(
        @Valid @RequestBody SendMessageRequest request
    ) {
        return chatMessageService.sendMessage(request);
    }
}
