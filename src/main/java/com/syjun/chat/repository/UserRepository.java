package com.syjun.chat.repository;

import com.syjun.chat.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    /** 根据用户名查找用户 */
    // 根据方法名自动推导 SQL 语句
    Optional<User> findByUsername(String username);

    List<User> findByNickname(String nickname);

    /** 判断用户名是否已存在 */
    boolean existsByUsername(String username);
}
