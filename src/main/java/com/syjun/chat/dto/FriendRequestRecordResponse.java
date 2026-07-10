package com.syjun.chat.dto;

import com.syjun.chat.entity.FriendRequestRecord;
import com.syjun.chat.entity.User;
import lombok.*;

/**
 * 好友请求记录响应体
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestRecordResponse {

    /** 当前记录的请求ID */
    private Long fromRecordId;

    /** 发起请求的用户ID */
    private Long fromUserId;

    /** 发起请求的用户名 */
    private String fromUsername;

    /** 发起请求的用户昵称 */
    private String fromUserNickname;

    public static FriendRequestRecordResponse from(
        FriendRequestRecord record,
        User fromUser
    ) {
        return FriendRequestRecordResponse.builder()
            .fromRecordId(record.getId())
            .fromUserId(fromUser.getId())
            .fromUsername(fromUser.getUsername())
            .fromUserNickname(fromUser.getNickname())
            .build();
    }
}
