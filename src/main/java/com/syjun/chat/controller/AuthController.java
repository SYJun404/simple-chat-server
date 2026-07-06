package com.syjun.chat.controller;

import com.syjun.chat.dto.*;
import com.syjun.chat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController // 接口方法返回对象，自动转换为json文本
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 注册 */
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        // @Valid DTO开启验证的关键
        // @RequestBody 代表传入的是json对象
        return userService.register(request);
    }

    /** 登录 */
    @PostMapping("/login")
    public ApiResponse<UserResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return userService.login(request);
    }

    // 退出登录
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(
        @Valid @RequestBody LogoutRequest request
    ) {
        return userService.logout(request);
    }
}
