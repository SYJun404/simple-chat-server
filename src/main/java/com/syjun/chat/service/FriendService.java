package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.Friend;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.FriendRepository;
import com.syjun.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * 获取好友列表（返回好友的详细信息）
     */
    public ApiResponse<List<UserResponse>> getFriendList(Long userId) {
        // 查该用户的所有好友关系
        List<Friend> friends = friendRepository.findByUserId(userId);

        // 根据 friend_id 查出好友的用户信息
        List<UserResponse> friendList = friends.stream().map(friend -> {
            User friendUser = userRepository.findById(friend.getFriendId()).orElse(null);
            return friendUser != null ? UserResponse.from(friendUser) : null;
        }).filter(u -> u != null).toList();

        return ApiResponse.success(friendList);
    }
}
