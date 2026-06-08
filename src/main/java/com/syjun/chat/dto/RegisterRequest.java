package com.syjun.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/** 注册请求 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 昵称，可选 */
    private String nickname;
}
