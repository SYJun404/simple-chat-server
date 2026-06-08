package com.syjun.chat.controller;

import com.syjun.chat.dto.*;
import com.syjun.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    /**
     * 查询两个用户之间的聊天记录
     * @param userId   当前用户ID
     * @param friendId 好友ID
     */
    @GetMapping("/history")
    public ApiResponse<List<ChatMessageResponse>> getChatHistory(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        return chatMessageService.getChatHistory(userId, friendId);
    }
}
