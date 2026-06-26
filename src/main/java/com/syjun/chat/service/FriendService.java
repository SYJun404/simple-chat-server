package com.syjun.chat.service;

import com.syjun.chat.customTcp.*;
import com.syjun.chat.dto.*;
import com.syjun.chat.entity.Friend;
import com.syjun.chat.entity.FriendRequestRecord;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.FriendRepository;
import com.syjun.chat.repository.FriendRequestRecordRepository;
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
    private final FriendRequestRecordService friendRequestRecordService;
    private final FriendRequestRecordRepository recordRepository;
    private final TcpServer customTcp;

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
     * 发送好友请求: 目标在线则 WebSocket 推送，不在线则存入数据库等待上线后拉取
     */

    public ApiResponse<Void> sendFriendSwingRequest(
        String fromUserNickname,
        String toUserNickname
    ) {
        List<User> fromUsers = userRepository.findByNickname(fromUserNickname);
        if (fromUsers.isEmpty()) {
            return ApiResponse.error(404, "发起者用户不存在");
        }
        User fromUser = fromUsers.getFirst();
        String fromUsername = fromUser.getUsername();

        // 获取目标用户信息

        List<User> toUsers = userRepository.findByNickname(toUserNickname);
        if (toUsers.isEmpty()) {
            return ApiResponse.error(404, "目标用户不存在");
        }
        User toUser = toUsers.getFirst();
        String toUsername = toUser.getUsername();

        if (toUsername.equals(fromUsername)) {
            return ApiResponse.error(404, "不能添加自己为好友!");
        }

        // 存入数据库
        friendRequestRecordService.saveFriendRequest(fromUsername, toUsername);

        if (customTcp.isOnline(toUsername)) {
            // 目标在线 → 通过 TCP 实时推送好友请求
            customTcp.sendFriendRequest(fromUser.getNickname(), toUsername);
            return ApiResponse.success("好友请求已发送", null);
        } else {
            return ApiResponse.error(404, "对方不在线，请求已保存");
        }
    }

    public ApiResponse<Void> sendFriendRequest(Long fromUserId, Long toUserId) {
        // 获取发起者的信息
        User fromUser = userRepository.findById(fromUserId).orElse(null);
        String fromNickname = fromUser != null ? fromUser.getNickname() : null;
        String fromAvatar = fromUser != null ? fromUser.getAvatar() : null;

        if (sessionManager.isOnline(toUserId)) {
            // 目标在线 → 通过 WebSocket 实时推送
            FriendRequestVO requestVO = FriendRequestVO.builder()
                .fromUserId(fromUserId)
                .fromNickname(fromNickname)
                .fromAvatar(fromAvatar)
                .build();

            sessionManager.sendToUser(
                toUserId,
                WsMessage.friendRequest(requestVO)
            );
            return ApiResponse.success("好友请求已发送", null);
        } else {
            // 目标不在线 → 存入数据库，等其上線後自行拉取
            // friendRequestRecordService.saveFriendRequest(fromUserId, toUserId);
            return ApiResponse.success(
                "对方不在线，好友请求已保存，待对方上线后处理",
                null
            );
        }
    }

    public ApiResponse<Void> acceptFriendSwingRequest(
        Long fromUserId,
        Long toUserId,
        Long recordId
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

        // 将好友申请，标记为已读
        FriendRequestRecord record = recordRepository
            .findById(recordId)
            .orElse(null);
        if (record != null) {
            record.setIsRead(1);
            recordRepository.save(record);
        }

        // 获取User对象
        User fromUser = userRepository.findById(fromUserId).orElse(null);
        User toUser = userRepository.findById(toUserId).orElse(null);

        customTcp.sendFriendAccept(
            fromUser.getUsername(),
            toUser.getUsername()
        );
        return ApiResponse.success("好友添加成功", null);
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
