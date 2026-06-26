package com.syjun.chat.service;

import com.syjun.chat.dto.*;
import com.syjun.chat.entity.FriendRequestRecord;
import com.syjun.chat.entity.User;
import com.syjun.chat.repository.FriendRequestRecordRepository;
import com.syjun.chat.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendRequestRecordService {

    private final FriendRequestRecordRepository recordRepository;
    private final UserRepository userRepository;

    /**
     * 保存离线好友请求记录
     */
    @Transactional
    public void saveFriendRequest(String fromUserId, String toUserId) {
        // 判断是否已存在相同的未处理请求，存在则直接返回
        if (
            recordRepository.existsByFromUserIdAndToUserIdAndIsRead(
                fromUserId,
                toUserId,
                0
            )
        ) {
            return;
        }

        FriendRequestRecord record = FriendRequestRecord.builder()
            .fromUserId(fromUserId)
            .toUserId(toUserId)
            .isRead(0)
            .build();
        recordRepository.save(record);
    }

    /**
     * 查询指定用户的所有未读好友请求
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<FriendRequestRecordResponse>> getPendingRequests(
        String userId
    ) {
        List<FriendRequestRecord> records =
            recordRepository.findByToUserIdAndIsRead(userId, 0);

        // 根据username查询User对象，组装响应
        List<FriendRequestRecordResponse> list = records
            .stream()
            .map(record -> {
                User fromUser = userRepository
                    .findByUsername(record.getFromUserId())
                    .orElse(null);
                return FriendRequestRecordResponse.from(record, fromUser);
            })
            .toList();

        return ApiResponse.success(list);
    }

    /**
     * 将好友请求记录标记为已读
     */
    @Transactional
    public ApiResponse<Void> markAsRead(Long recordId) {
        FriendRequestRecord record = recordRepository
            .findById(recordId)
            .orElse(null);
        if (record == null) {
            return ApiResponse.error(404, "请求记录不存在");
        }
        record.setIsRead(1);
        recordRepository.save(record);
        return ApiResponse.success("已标记为已读", null);
    }

    /**
     * 将某个用户发给另一个用户的所有未读请求标记为已读
     */
    @Transactional
    public void markAllAsRead(String toUserId, String fromUserId) {
        List<FriendRequestRecord> records =
            recordRepository.findByToUserIdAndIsRead(toUserId, 0);

        records
            .stream()
            .filter(r -> r.getFromUserId().equals(fromUserId))
            .forEach(r -> r.setIsRead(1));

        recordRepository.saveAll(records);
    }
}
