package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.Friend;
import com.syjun.chat.entity.FriendRequestRecord;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.FriendRepository;
import com.syjun.chat.repository.FriendRequestRecordRepository;
import com.syjun.chat.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final FriendRequestRecordService friendRequestRecordService;
    private final FriendRequestRecordRepository recordRepository;
    private final MessagePushService messagePushService;

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
     * 发送好友请求（按用户ID查找，浏览器/App 端使用）
     */
    public ApiResponse<Long> sendFriendRequest(Long fromUserId, Long toUserId) {
        User fromUser = userRepository.findById(fromUserId).orElse(null);
        if (fromUser == null) {
            return ApiResponse.error(404, "发起者用户不存在");
        }
        User toUser = userRepository.findById(toUserId).orElse(null);
        if (toUser == null) {
            return ApiResponse.error(404, "目标用户不存在");
        }
        return doSendFriendRequest(fromUser, toUser);
    }

    /**
     * 发送好友请求（按昵称查找，Swing 客户端使用）
     */
    public ApiResponse<Long> sendFriendSwingRequest(
        String fromUserNickname,
        String toUserNickname
    ) {
        List<User> fromUsers = userRepository.findByNickname(fromUserNickname);
        if (fromUsers.isEmpty()) {
            return ApiResponse.error(404, "发起者用户不存在");
        }
        List<User> toUsers = userRepository.findByNickname(toUserNickname);
        if (toUsers.isEmpty()) {
            return ApiResponse.error(404, "目标用户不存在");
        }
        return doSendFriendRequest(fromUsers.getFirst(), toUsers.getFirst());
    }

    /**
     * 核心：发送好友请求
     * 保存记录 → 判断在线 → 推送
     */
    private ApiResponse<Long> doSendFriendRequest(User fromUser, User toUser) {
        if (toUser.getUsername().equals(fromUser.getUsername())) {
            return ApiResponse.error(400, "不能添加自己为好友");
        }

        // 存入数据库
        Long recordId = friendRequestRecordService.saveFriendRequest(
            fromUser.getUsername(),
            toUser.getUsername()
        );
        if (recordId.equals(-1L)) {
            return ApiResponse.error(400, "当前好友添加记录已经存在");
        }

        if (
            messagePushService.isTcpOnline(toUser.getUsername()) ||
            messagePushService.isWsOnline(toUser.getUsername())
        ) {
            FriendRequestVO requestVO = FriendRequestVO.builder()
                .fromUserId(fromUser.getId())
                .fromNickname(fromUser.getNickname())
                .fromRecordId(recordId)
                .build();

            messagePushService.sendFriendRequest(
                fromUser.getNickname(),
                toUser.getUsername(),
                requestVO
            );
            return ApiResponse.success("好友请求已发送", recordId);
        }

        return ApiResponse.error(404, "对方不在线，请求已保存");
    }

    /**
     * 核心：同意好友请求
     * 双向建立好友关系 → 标记请求已读 → 推送通知
     */
    public ApiResponse<Void> acceptFriendRequest(
        Long fromUserId,
        Long toUserId,
        Long recordId
    ) {
        if (friendRepository.existsByUserIdAndFriendId(toUserId, fromUserId)) {
            return ApiResponse.error(400, "已经是好友了");
        }

        friendRepository.save(
            Friend.builder().userId(toUserId).friendId(fromUserId).build()
        );
        friendRepository.save(
            Friend.builder().userId(fromUserId).friendId(toUserId).build()
        );

        // 将好友申请标记为已读
        FriendRequestRecord record = recordRepository
            .findById(recordId)
            .orElse(null);
        if (record != null) {
            record.setIsRead(1);
            recordRepository.save(record);
        }

        User fromUser = userRepository.findById(fromUserId).orElse(null);
        User toUser = userRepository.findById(toUserId).orElse(null);

        if (fromUser != null && toUser != null) {
            messagePushService.sendFriendAccept(
                fromUser.getUsername(),
                toUser.getUsername(),
                toUserId,
                fromUserId
            );
        }

        return ApiResponse.success("好友添加成功", null);
    }
}
