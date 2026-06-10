package com.syjun.chat.dto;

import com.syjun.chat.entity.User;
import lombok.*;

/** 用户信息响应 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;

    /** 从实体转换，不暴露密码 */
    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .status(user.getStatus())
            .avatar(user.getAvatar())
            .build();
    }
}
