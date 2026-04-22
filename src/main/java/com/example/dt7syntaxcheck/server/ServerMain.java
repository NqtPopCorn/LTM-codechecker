package com.example.dt7syntaxcheck.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerMain {

    private static final int DATA_PORT = 5000; // Port nhận yêu cầu kết nối mới
    private static final int BUFFER_SIZE = 256;

    public static void main(String[] args) throws Exception {

        // Lấy RSA key pair (giữ nguyên logic cũ)
        KeyManager keyManager = new KeyManager();
        KeyManager.RSAKeyPairs rsaKeyPair = KeyManager.initializeKeys();

        // ── Khởi động BroadcastListener UDP socket(port 4999)
        // ───────────────────────────
        new BroadcastListener().start();

        // ── Vòng lặp chính TCP socket : lắng nghe "HELLO" trên port 5000
        // ─────────────────
        System.out.println("[SERVER] TCP Server đang lắng nghe trên port " + DATA_PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(DATA_PORT)) {
            while (true) {
                // Lệnh accept() sẽ chặn luồng tại đây cho đến khi có 1 Client gõ cửa
                Socket clientSocket = serverSocket.accept();

                String clientIP = clientSocket.getInetAddress().getHostAddress();
                int clientPort = clientSocket.getPort();
                System.out.println("[+] Phát hiện Client mới kết nối từ: " + clientIP + ":" + clientPort);

                // Tạo thread xử lý client này, non-bloking
                ClientHandler clientHandler = new ClientHandler(clientSocket, rsaKeyPair);
                clientHandler.start();
            }

        }
    }
}