package com.syjun.chat.repository;

import com.syjun.chat.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 继承了 JpaRepository 就相当于拿到了 Spring Data 的“免死金牌”，
// Spring 会罩着它并自动帮它完成注册，所以显式写 @Repository 属于画龙点睛（可写可不写），不写也不会有任何问题。
public interface UserRepository extends JpaRepository<User, Long> {
    /** 根据用户名查找用户 */
    // 根据方法名自动推导 SQL 语句
    Optional<User> findByUsername(String username);

    List<User> findByNickname(String nickname);

    /** 判断用户名是否已存在 */
    boolean existsByUsername(String username);
}
