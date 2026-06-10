package com.syjun.chat.controller;

import com.syjun.chat.dto.*;
import com.syjun.chat.service.FriendService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 获取好友列表 */
    @GetMapping("/get")
    public ApiResponse<List<UserResponse>> getFriendList(
        @RequestParam Long userId
    ) {
        return friendService.getFriendList(userId);
    }

    /** 按昵称搜索用户 */
    @GetMapping("/search")
    public ApiResponse<List<UserResponse>> searchFromNickname(
        @RequestParam String nickname
    ) {
        return friendService.searchFriend(nickname);
    }

    /**
     * 发送好友请求: 校验在线 + WebSocket 推送
     */
    @PostMapping("/request")
    public ApiResponse<Void> sendFriendRequest(
        @Valid @RequestBody FriendRequestDTO dto
    ) {
        return friendService.sendFriendRequest(
            dto.getFromUserId(),
            dto.getToUserId()
        );
    }

    /** 接受好友请求: 双向入库 + WebSocket 推送 */
    @PostMapping("/accepted")
    public ApiResponse<Void> acceptFriendRequest(
        @Valid @RequestBody FriendRequestDTO dto
    ) {
        return friendService.acceptFriendRequest(
            dto.getFromUserId(),
            dto.getToUserId()
        );
    }

    /** 好友/接受 请求 DTO */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class FriendRequestDTO {

        @NotNull
        private Long fromUserId;

        @NotNull
        private Long toUserId;
    }
}
