package com.syjun.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/** 登录请求 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "用户名不能为空") // 参数验证
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
