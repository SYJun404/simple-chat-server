package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.Friend;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.FriendRepository;
import com.syjun.chat.repository.UserRepository;
import com.syjun.chat.websocket.WebSocketSessionManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final WebSocketSessionManager sessionManager;

    /**
     * 获取好友列表（返回好友的详细信息）
     */
    public ApiResponse<List<UserResponse>> getFriendList(Long userId) {
        // 查该用户的所有好友关系
        List<Friend> friends = friendRepository.findByUserId(userId);

        // 根据 friend_id 查出好友的用户信息
        List<UserResponse> friendList = friends
            .stream()
            .map(friend -> {
                User friendUser = userRepository
                    .findById(friend.getFriendId())
                    .orElse(null);
                return friendUser != null
                    ? UserResponse.from(friendUser)
                    : null;
            })
            .filter(u -> u != null)
            .toList();

        return ApiResponse.success(friendList);
    }

    /** 按昵称搜索用户 */
    public ApiResponse<List<UserResponse>> searchFriend(String nickname) {
        List<User> users = userRepository.findByNickname(nickname);

        List<UserResponse> result = users
            .stream()
            .map(UserResponse::from)
            .toList();

        return ApiResponse.success(result);
    }

    /**
     * 发送好友请求: 校验目标用户在线 → 通过 WebSocket 推送请求消息
     */
    public ApiResponse<Void> sendFriendRequest(Long fromUserId, Long toUserId) {
        // 校验目标用户是否在线
        if (!sessionManager.isOnline(toUserId)) {
            return ApiResponse.error(400, "对方不在线，无法发送好友请求");
        }

        // 获取发起者的昵称
        User fromUser = userRepository.findById(fromUserId).orElse(null);
        String fromNickname = fromUser.getNickname();
        String fromAvatar = fromUser.getAvatar();
        // 构建好友请求消息
        FriendRequestVO requestVO = FriendRequestVO.builder()
            .fromUserId(fromUserId)
            .fromNickname(fromNickname)
            .fromAvatar(fromAvatar)
            .build();

        // 通过 WebSocket 推送给目标用户
        sessionManager.sendToUser(toUserId, WsMessage.friendRequest(requestVO));

        return ApiResponse.success("好友请求已发送", null);
    }

    /**
     * 同意好友请求: 更新数据库内容 → 通过 WebSocket 推送刷新好友列表消息
     */
    public ApiResponse<Void> acceptFriendRequest(
        Long fromUserId,
        Long toUserId
    ) {
        if (friendRepository.existsByUserIdAndFriendId(toUserId, fromUserId)) {
            return ApiResponse.error(400, "已经是好友了");
        }

        Friend f1 = Friend.builder()
            .userId(toUserId)
            .friendId(fromUserId)
            .build();
        Friend f2 = Friend.builder()
            .userId(fromUserId)
            .friendId(toUserId)
            .build();
        friendRepository.save(f1);
        friendRepository.save(f2);

        sessionManager.sendToUser(
            toUserId,
            WsMessage.friendAccepted(fromUserId)
        );
        sessionManager.sendToUser(
            fromUserId,
            WsMessage.friendAccepted(toUserId)
        );

        return ApiResponse.success("好友添加成功", null);
    }
}
