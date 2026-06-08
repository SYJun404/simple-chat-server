package com.syjun.chat.repository;

import com.syjun.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 查询两人之间的聊天记录（双向），按时间升序
     */
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.fromUserId = :user1 AND m.toUserId = :user2)
           OR (m.fromUserId = :user2 AND m.toUserId = :user1)
        ORDER BY m.sendTime ASC
    """)
    List<ChatMessage> findChatHistory(@Param("user1") Long user1,
                                      @Param("user2") Long user2);

    /** 查询某个用户的所有未读消息 */
    List<ChatMessage> findByToUserIdAndIsRead(Long toUserId, Integer isRead);
}
