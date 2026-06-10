package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.UserRepository;
import com.syjun.chat.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WebSocketSessionManager sessionManager;

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

        // 创建用户（默认离线）
        User user = User.builder()
            .username(usname)
            .password(request.getPassword())
            .nickname(nkname != null ? nkname : usname)
            .avatar("https://api.dicebear.com/10.x/micah/svg?seed=" + nkname)
            .status(0)
            .build();

        user = userRepository.save(user);

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
        sessionManager.broadcast(
            WsMessage.builder()
                .type("friend_accepted")
                .data(user.getId())
                .build()
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
        sessionManager.broadcast(
            WsMessage.builder()
                .type("friend_accepted")
                .data(user.getId())
                .build()
        );

        return ApiResponse.success(true);
    }
}
