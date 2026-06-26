package com.syjun.chat.repository;

import com.syjun.chat.entity.FriendRequestRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestRecordRepository
    extends JpaRepository<FriendRequestRecord, Long>
{
    /** 查询某个用户的所有未读好友请求 */
    List<FriendRequestRecord> findByToUserIdAndIsRead(
        String toUserId,
        Integer isRead
    );

    /** 查询某个用户的所有好友请求记录 */
    List<FriendRequestRecord> findByToUserId(String toUserId);

    /** 查询两人之间是否有未处理的请求 */
    boolean existsByFromUserIdAndToUserIdAndIsRead(
        String fromUserId,
        String toUserId,
        Integer isRead
    );
}
