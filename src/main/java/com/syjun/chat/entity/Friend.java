package com.syjun.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friend",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 本人ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 好友ID */
    @Column(name = "friend_id", nullable = false)
    private Long friendId;
}
