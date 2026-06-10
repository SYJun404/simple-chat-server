package com.syjun.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/** 登出请求 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;
}
