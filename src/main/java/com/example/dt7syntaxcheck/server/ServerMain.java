package com.example.dt7syntaxcheck.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.dt7syntaxcheck.share.ServiceRegistry;

public class ServerMain {

    // Định nghĩa cổng giao tiếp cho Server. Phải khớp với cổng mà ClientService đang gọi tới.
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   SERVER KIỂM TRA VÀ THỰC THI CODE   ");
        System.out.println("=================================================");
        System.out.println("Đang khởi động hệ thống...");

        // =====================================================
        // IMPROVEMENT: Per-Client Key Pair
        // =====================================================
        System.out.println("[INFO] Mode: Per-Client Key Pair (ENABLED)");
        System.out.println("[INFO] Mỗi client connection → sinh unique RSA key pair");
        System.out.println("[INFO] ✓ Key isolation, ✓ Better security\n");

        // Khởi tạo ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[INFO] Server đang lắng nghe kết nối tại port " + PORT + "...\n");

            // =====================================================
            // ĐĂNG KÝ SERVER IP LÊN GITHUB GIST
            // =====================================================
            System.out.println("[INFO] Đang đăng ký server IP lên GitHub Gist...");
            boolean registrationSuccess = ServiceRegistry.registerServer(PORT);
            if (!registrationSuccess) {
                System.out.println("[!] Lưu ý: Không thể đăng ký server trên GitHub Gist");
                System.out.println("[!] Client sẽ cần kết nối trực tiếp qua localhost hoặc IP cố định");
            }
            System.out.println();

            // Vòng lặp vô hạn để giữ Server luôn mở và sẵn sàng đón nhiều Client
            while (true) {
                // Lệnh accept() sẽ chặn luồng tại đây cho đến khi có 1 Client gõ cửa
                Socket clientSocket = serverSocket.accept();

                String clientIP = clientSocket.getInetAddress().getHostAddress();
                int clientPort = clientSocket.getPort();
                System.out.println("[+] Phát hiện Client mới kết nối từ: " + clientIP + ":" + clientPort);

                // =====================================================
                // IMPROVEMENT: Mỗi ClientHandler sinh RSA key pair riêng
                // =====================================================
                // Multithread: Giao socket cho ClientHandler, MainThread tiếp tục đón client khác
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            }

        } catch (IOException e) {
            System.err.println("[-] Lỗi Server (Cổng " + PORT + " có thể đang bị chiếm dụng): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
