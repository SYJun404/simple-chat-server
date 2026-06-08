package com.syjun.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录账号，唯一 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 密码 */
    @Column(nullable = false, length = 100)
    private String password;

    /** 昵称 */
    @Column(length = 50)
    private String nickname;

    /** 在线状态: 0-离线, 1-在线 */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;
}
