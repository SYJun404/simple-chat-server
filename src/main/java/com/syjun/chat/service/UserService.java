package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service // spring's bean
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MessagePushService messagePushService;

    /**
     * 注册
     * @return 注册成功的用户信息
     */
    public ApiResponse<UserResponse> register(RegisterRequest request) {
        String usname = request.getUsername();
        String nkname = request.getNickname();

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(usname)) {
            return ApiResponse.error(400, "用户名已存在");
        }

        // 创建用户（默认离线，先不设头像）
        User user = User.builder()
            .username(usname)
            .password(request.getPassword())
            .nickname(nkname != null ? nkname : usname)
            .status(0)
            .build();

        user = userRepository.save(user);

        // 根据 id 分配头像
        String[] avatars = {
            "https://img.cdn1.vip/i/6a2a6a21a6f30_1781164577.png",
            "https://img.cdn1.vip/i/6a2a6a215d081_1781164577.png",
            "https://img.cdn1.vip/i/6a2a6a2160b63_1781164577.png",
            "https://img.cdn1.vip/i/6a2a6a210d551_1781164577.png",
            "https://img.cdn1.vip/i/6a2a6a210dfe9_1781164577.png",
        };
        user.setAvatar(avatars[(int) (user.getId() % 5)]);
        userRepository.save(user);

        return ApiResponse.success("注册成功", UserResponse.from(user));
    }

    /**
     * 登录
     * @return 登录成功后的用户信息
     */
    public ApiResponse<UserResponse> login(LoginRequest request) {
        User user = userRepository
            .findByUsername(request.getUsername())
            .orElse(null);

        if (user == null) {
            return ApiResponse.error(400, "用户不存在");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return ApiResponse.error(400, "密码错误");
        }

        // 登录成功，更新在线状态
        user.setStatus(1);
        userRepository.save(user);

        // 通知所有在线用户刷新好友列表
        messagePushService.broadcast(
            WsMessage.builder()
                .type("friend_accepted")
                .data(user.getId())
                .build(),
            "LOGIN|" + user.getUsername()
        );

        return ApiResponse.success("登录成功", UserResponse.from(user));
    }

    // 退出登录，成功返回true，错误返回false
    public ApiResponse<Boolean> logout(LogoutRequest request) {
        User user = userRepository
            .findByUsername(request.getUsername())
            .orElse(null);

        if (user == null) {
            return ApiResponse.error(400, "用户不存在");
        }

        // 退出成功，更新在线状态
        user.setStatus(0);
        userRepository.save(user);

        // 通知所有在线用户刷新好友列表
        messagePushService.broadcast(
            WsMessage.builder()
                .type("friend_accepted")
                .data(user.getId())
                .build(),
            "LOGOUT|" + user.getUsername()
        );

        return ApiResponse.success(true);
    }
}
