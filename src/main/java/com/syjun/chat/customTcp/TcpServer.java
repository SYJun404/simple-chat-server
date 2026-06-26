package com.syjun.chat.customTcp;

import com.syjun.chat.dto.*;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Stream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TcpServer implements CommandLineRunner {

    private static final int PORT = 8412;

    private ServerSocket serverSocket;

    // 用户名 → Socket输出流
    private final Map<String, PrintWriter> clients = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        serverSocket = new ServerSocket(PORT);
        System.out.println("TCP Server 启动，端口 " + PORT);

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
                System.out.println("TCP Server 已关闭");
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
                System.out.println(username + " 断开连接");
            } finally {
                if (username != null) {
                    clients.remove(username);
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
                    username = parts[1];
                    clients.put(username, out);

                    // 广播给所有其他在线用户：新用户上线
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

                    System.out.println(username + " 已上线");
                }
                case "LOGOUT" -> {
                    clients.remove(username);

                    // 广播给其他在线用户
                    for (Map.Entry<
                        String,
                        PrintWriter
                    > entry : clients.entrySet()) {
                        entry
                            .getValue()
                            .println("SERVER|USER_LOGOUT|" + username);
                    }

                    System.out.println(username + " 已登出");
                }
                default -> out.println("SERVER|UNKNOWN_CMD");
            }
        }
    }

    // MSG|{"id":27,"fromUserId":4,"toUserId":2,"content":"4","sendTime":"2026-06-26T11:10:58.108211","isRead":0}
    public void sendToUser(String toUsername, ChatMessageResponse message) {
        PrintWriter target = clients.get(toUsername);
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
