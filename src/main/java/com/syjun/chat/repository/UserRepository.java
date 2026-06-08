package com.syjun.chat.repository;

import com.syjun.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 根据用户名查找用户 */
    Optional<User> findByUsername(String username);

    /** 判断用户名是否已存在 */
    boolean existsByUsername(String username);
}
