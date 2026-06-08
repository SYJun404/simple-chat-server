package com.syjun.chat.controller;

import com.syjun.chat.dto.*;
import com.syjun.chat.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 获取好友列表 */
    @GetMapping
    public ApiResponse<List<UserResponse>> getFriendList(@RequestParam Long userId) {
        return friendService.getFriendList(userId);
    }
}
