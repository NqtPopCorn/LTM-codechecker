package com.example.dt7syntaxcheck.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    // Định nghĩa cổng giao tiếp cho Server. Phải khớp với cổng mà ClientService
    // đang gọi tới.
    private static final int PORT = 5000;

    // Lưu RSA key pairs cho mã hóa lai
    private static KeyManager.RSAKeyPair rsaKeyPair;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   SERVER KIỂM TRA VÀ THỰC THI CODE   ");
        System.out.println("=================================================");
        System.out.println("Đang khởi động hệ thống...");

        // Khởi tạo RSA keys cho mã hóa lai
        try {
            rsaKeyPair = KeyManager.initializeKeys();
            System.out.println("[INFO] RSA keys đã khởi tạo thành công!\n");
        } catch (Exception e) {
            System.err.println("[-] Lỗi khởi tạo RSA keys: " + e.getMessage());
            e.printStackTrace();
            return;
        }

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

                // Multithread: Giao socket cho ClientHandler, MainThread tiếp tục đón client
                // khác
                ClientHandler clientHandler = new ClientHandler(clientSocket, rsaKeyPair);
                clientHandler.start();
            }

        } catch (IOException e) {
            System.err.println("[-] Lỗi Server (Cổng " + PORT + " có thể đang bị chiếm dụng): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
