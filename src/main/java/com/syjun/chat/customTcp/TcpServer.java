package com.syjun.chat.customTcp;

import com.syjun.chat.dto.*;
import com.syjun.chat.repository.UserRepository;
import com.syjun.chat.service.OnlineStatusManager;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TcpServer implements CommandLineRunner {

    private static final int PORT = 8412;

    private ServerSocket serverSocket;

    // 用户名 → Socket输出流
    private final Map<String, PrintWriter> clients = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final OnlineStatusManager onlineStatusManager;

    @Override
    public void run(String... args) throws Exception {
        serverSocket = new ServerSocket(PORT);
        log.info("TCP Server 启动，端口 {}", PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                log.info("TCP Server 已关闭");
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private class ClientHandler implements Runnable {

        private final Socket socket;
        private String username;
        private PrintWriter out;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );
                PrintWriter writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
                )
            ) {
                this.out = writer;
                String line;
                while ((line = in.readLine()) != null) {
                    handleMessage(line);
                }
            } catch (IOException e) {
                log.warn(username + " 断开连接" + "--->" + e);
            } finally {
                if (username != null) {
                    clients.remove(username);
                    log.warn(username + "【 finally 】 断开连接");
                    // 用户下线，将status设置为0
                    userRepository.findByUsername(username).ifPresent(user -> {
                        if (!onlineStatusManager.isUserOnline(username)) {
                            user.setStatus(0);
                            userRepository.save(user);
                        }
                    });
                }
            }
        }

        //   LOGIN|1004
        //   LOGOUT

        private void handleMessage(String raw) {
            String[] parts = raw.split("\\|", -1);
            String cmd = parts[0];

            switch (cmd) {
                case "LOGIN" -> {
                    if (parts.length < 2 || parts[1].isBlank()) {
                        log.warn("LOGIN 格式错误: {}", raw);
                        return;
                    }
                    username = parts[1];
                    clients.put(username, out);

                    log.info("{} 已上线", username);
                }
                case "LOGOUT" -> {
                    clients.remove(username);

                    log.info("{} 已登出", username);
                }
                default -> out.println("SERVER|UNKNOWN_CMD");
            }
        }
    }

    public void broadcast(String message) {
        String[] parts = message.split("\\|", -1);
        String cmd = parts[0];
        String username = parts[1];

        switch (cmd) {
            case "LOGIN" -> {
                for (Map.Entry<
                    String,
                    PrintWriter
                > entry : clients.entrySet()) {
                    if (!entry.getKey().equals(username)) {
                        entry
                            .getValue()
                            .println("SERVER|USER_LOGIN|" + username);
                    }
                }
            }
            case "LOGOUT" -> {
                for (Map.Entry<
                    String,
                    PrintWriter
                > entry : clients.entrySet()) {
                    entry.getValue().println("SERVER|USER_LOGOUT|" + username);
                }
            }
        }
    }

    /** MSG|{"id":27,"fromUserId":4,"toUserId":2,"content":"4","sendTime":"2026-06-26T11:10:58.108211","isRead":0} */
    public void sendToUser(String toUsername, ChatMessageResponse message) {
        // toUsername可能是1004，也可能是1004@
        // 先在 clients map 中查找存在的值，再进行消息转发，如何同时存在都要发送
        if (toUsername == null || toUsername.isEmpty()) return;

        // 构造出Android端需要发送的目标 Key
        // Swing 客户端在 clients map 中的 key 是 "username"，Android 是 "username@"
        String androidUsername = toUsername + "@";

        // 准备好要序列化的消息内容
        String payload = "MSG|" + message.toJsonStr();

        // 统一遍历发送
        for (String key : new String[] { toUsername, androidUsername }) {
            PrintWriter target = clients.get(key);
            if (target != null) {
                target.println(payload);
            }
        }
    }

    /** 给 sender 的另一个平台发消息。platformType: 1=Swing, 2=Android */
    public void sendToSelfDiffPlatform(
        String fromUsername,
        ChatMessageResponse message,
        int platformType
    ) {
        if (fromUsername == null || fromUsername.isEmpty()) return;

        String targetKey =
            platformType != 2 ? fromUsername + "@" : fromUsername;

        PrintWriter target = clients.get(targetKey);
        if (target != null) {
            target.println("MSG|" + message.toJsonStr());
        }
    }

    /** 通过username向特定的用户发送加好友的消息 */
    public void sendFriendRequest(String fromUserNickname, String toUsername) {
        PrintWriter target = clients.get(toUsername);
        if (target != null) {
            target.println("SERVER|FRIEND_REQUEST|" + fromUserNickname);
        }
    }

    /** 发送接收好友的消息 */
    public void sendFriendAccept(String fromUsername, String toUsername) {
        Stream.of(toUsername, fromUsername)
            .map(clients::get)
            .filter(Objects::nonNull)
            .forEach(target -> target.println("SERVER|FRIEND_ACCEPT"));
    }

    /** 通过username判断是否在线 */
    public boolean isOnline(String username) {
        return clients.containsKey(username);
    }
}
