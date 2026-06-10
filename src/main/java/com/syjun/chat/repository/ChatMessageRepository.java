package com.syjun.chat.repository;

import com.syjun.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository
    extends JpaRepository<ChatMessage, Long>
{
    /**
     * 查询聊天记录
     */
    @Query(
        """
            SELECT m FROM ChatMessage m
            WHERE (m.fromUserId = :user1)
               OR (m.toUserId = :user1)
            ORDER BY m.sendTime ASC
        """
    )
    List<ChatMessage> findChatHistory(@Param("user1") Long user1);

    /** 查询某个用户的所有未读消息 */
    List<ChatMessage> findByToUserIdAndIsRead(Long toUserId, Integer isRead);
}
