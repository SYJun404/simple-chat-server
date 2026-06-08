package com.syjun.chat.repository;

import com.syjun.chat.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    /** 查询某用户的所有好友关系 */
    List<Friend> findByUserId(Long userId);

    /** 判断两人是否已经是好友 */
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}
