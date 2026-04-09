package com.example.dt7syntaxcheck.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    // Định nghĩa cổng giao tiếp cho Server. Phải khớp với cổng mà ClientService đang gọi tới.
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   SERVER KIỂM TRA VÀ THỰC THI CODE   ");
        System.out.println("=================================================");
        System.out.println("Đang khởi động hệ thống...");

        // Khởi tạo ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[INFO] Server đang lắng nghe kết nối tại port " + PORT + "...\n");

            // Vòng lặp vô hạn để giữ Server luôn mở và sẵn sàng đón nhiều Client
            while (true) {
                // Lệnh accept() sẽ chặn luồng tại đây cho đến khi có 1 Client gõ cửa
                Socket clientSocket = serverSocket.accept();

                String clientIP = clientSocket.getInetAddress().getHostAddress();
                int clientPort = clientSocket.getPort();
                System.out.println("[+] Phát hiện Client mới kết nối từ: " + clientIP + ":" + clientPort);

                // Kỹ thuật Multithread: 
                // Giao ngay socket của Client vừa kết nối cho một luồng (Thread) ClientHandler xử lý.
                // Luồng chính (ServerMain) lập tức quay lại vòng lặp while để đón người tiếp theo.
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            }

        } catch (IOException e) {
            System.err.println("[-] Lỗi Server (Cổng " + PORT + " có thể đang bị chiếm dụng): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
