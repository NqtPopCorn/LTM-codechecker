package com.example.dt7syntaxcheck.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerMain {

    private static final int DATA_PORT = 5000; // Port nhận yêu cầu kết nối mới
    // private static final int BUFFER_SIZE = 256;

    public static void main(String[] args) throws Exception {

        // Lấy RSA key pair (giữ nguyên logic cũ)
        KeyManager keyManager = new KeyManager();
        KeyManager.RSAKeyPairs rsaKeyPair = KeyManager.initializeKeys();

        startDiscoveryBeacon();
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

    private static final int BROADCAST_PORT = 4999;

    private static void startDiscoveryBeacon() {
        Thread beaconThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                String message = "TIKI_SERVER_DISCOVERY:" + DATA_PORT;
                byte[] buffer = message.getBytes();

                System.out.println("[SYSTEM] Discovery Beacon started (UDP Broadcast).");

                while (true) {
                    DatagramPacket packet = new DatagramPacket(
                            buffer, buffer.length,
                            InetAddress.getByName("255.255.255.255"), BROADCAST_PORT);
                    socket.send(packet);
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Discovery Beacon failed: " + e.getMessage());
            }
        });
        beaconThread.setDaemon(true);
        beaconThread.start();
    }
}