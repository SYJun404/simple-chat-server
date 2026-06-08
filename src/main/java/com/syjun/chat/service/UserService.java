package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	/**
	 * 注册
	 * @return 注册成功的用户信息
	 */
	public ApiResponse<UserResponse> register(RegisterRequest request) {
		// 检查用户名是否已存在
		if (userRepository.existsByUsername(request.getUsername())) {
			return ApiResponse.error(400, "用户名已存在");
		}

		// 创建用户（默认离线）
		User user = User.builder()
			.username(request.getUsername())
			.password(request.getPassword())
			.nickname(
				request.getNickname() != null
					? request.getNickname()
					: request.getUsername()
			)
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

		return ApiResponse.success("登录成功", UserResponse.from(user));
	}
}
