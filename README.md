# Simple Chat Server

## 目录结构

```
src/main/java/com/syjun/chat/
├── ChatApplication.java          # 应用入口
│
├── config/
│   ├── SecurityConfig.java       # Spring Security 配置（关闭 CSRF，放行所有请求）
│   └── WebSocketConfig.java      # WebSocket 端点注册（/ws/chat）
│
├── controller/
│   ├── AuthController.java       # 登录 / 注册 API
│   ├── ChatController.java       # 聊天消息发送 / 历史记录 API
│   └── FriendController.java     # 好友搜索 / 请求 / 同意 API
│
├── customTcp/
│   └── TcpServer.java            # 自定义 TCP 服务（端口 8412），供 Swing/Android 客户端连接
│
├── dto/                          # 请求 & 响应数据模型
│   ├── ApiResponse.java          # 统一响应格式
│   ├── ChatMessageResponse.java  # 聊天消息响应
│   ├── FriendRequestRecordResponse.java
│   ├── FriendRequestVO.java      # 好友请求推送体
│   ├── LoginRequest.java
│   ├── LogoutRequest.java
│   ├── RegisterRequest.java
│   ├── SendMessageRequest.java
│   ├── UserResponse.java
│   └── WsMessage.java            # WebSocket 推送消息体
│
├── entity/                       # JPA 实体
│   ├── ChatMessage.java          # 聊天消息表
│   ├── Friend.java               # 好友关系表
│   ├── FriendRequestRecord.java  # 好友申请表
│   └── User.java                 # 用户表
│
├── repository/                   # JPA 数据访问层
│   ├── ChatMessageRepository.java
│   ├── FriendRepository.java
│   ├── FriendRequestRecordRepository.java
│   └── UserRepository.java
│
├── service/                      # 业务逻辑层
│   ├── ChatMessageService.java   # 聊天消息收发
│   ├── FriendRequestRecordService.java
│   ├── FriendService.java        # 好友管理
│   ├── MessagePushService.java   # 统一推送（WebSocket + TCP 双通道）
│   └── UserService.java          # 用户登录 / 注册 / 登出
│
└── websocket/
    ├── ChatWebSocketHandler.java      # WebSocket 连接处理器
    └── WebSocketSessionManager.java   # WebSocket 会话管理（注册 / 移除 / 推送）

src/main/resources/
└── application.properties        # 应用配置（数据库、端口等）
```

## 推送架构

```
浏览器 (WebSocket) ──→ WebSocketSessionManager ──→ MessagePushService ──→ 接收方
Swing/Android (TCP) ──→ TcpServer (:8412)       ──→                     (双通道)
```
