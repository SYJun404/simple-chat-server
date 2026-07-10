package com.syjun.chat.dto;

import lombok.*;

/**
 * 好友请求消息体（通过 WebSocket 推送给目标用户）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestVO {

    private Long fromRecordId;

    /** 发起请求的用户ID */
    private Long fromUserId;

    /** 发起请求的用户昵称 */
    private String fromNickname;
}
