package com.example.dt7syntaxcheck.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import com.example.dt7syntaxcheck.share.HybridCryptoManager;
import com.example.dt7syntaxcheck.share.RequestPayload;
import com.example.dt7syntaxcheck.share.ResponsePayload;
import com.google.gson.Gson;

public class ClientService {

    private static final int BROADCAST_PORT = 4999;
    private static final int SERVER_TCP_PORT = 5000;
    private static final int TIMEOUT_MS = 10000;

    private final Gson gson = new Gson();
    private HybridCryptoManager hybridCryptoManager;
    private javax.crypto.SecretKey sessionKeyPlaintext;
    private InetAddress serverAddr = null;

    public ClientService() {
    }

    public ResponsePayload sendCodeToServer(RequestPayload requestPayload) throws Exception {
        // System.out.println("[Server]: " + serverAddr);
        // ─── 1. DISCOVERY (UDP) ─────────────────────────────
        // if (serverAddr == null) {
        serverAddr = InetAddress.getByName(ClientService.discoverServerIp());
        // }
        // System.out.println("[Server]: " + serverAddr);
        // return null;
        // serverAddr = InetAddress.getByName("192.186.43.172");

        // ─── 2. TCP CONNECT ────────────────────────────────
        try (Socket socket = new Socket(serverAddr, SERVER_TCP_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // ============================================
            // BƯỚC 1: KEY EXCHANGE - NHẬN PUBLIC KEY TỪ SERVER
            // ============================================
            String publicKeyFromServer = in.readLine();
            if (publicKeyFromServer == null) {
                throw new Exception("Mất kết nối với Server!");
            }
            System.out.println("[CLIENT] ✓ Nhận public key từ server (Key Exchange thành công!)");

            // Khởi tạo HybridCryptoManager với public key từ server
            this.hybridCryptoManager = new HybridCryptoManager(publicKeyFromServer);

            // ============================================
            // BƯỚC 2: MÃ HÓA HYBRID - CHUẨN BỊ DỮ LIỆU GỬI
            // ============================================
            String jsonPayload = gson.toJson(requestPayload);
            HybridCryptoManager.HybridEncryptedMessageWithKey encryptedMessage = hybridCryptoManager
                    .encryptHybridWithKey(jsonPayload);
            this.sessionKeyPlaintext = encryptedMessage.getSessionKey(); // LƯU SESSION KEY
            System.out.println("[CLIENT] ✓ Mã hóa Hybrid thành công!");
            System.out.println("[CLIENT] → Session Key (RSA): " +
                    encryptedMessage.getEncryptedSessionKey().substring(0,
                            Math.min(50, encryptedMessage.getEncryptedSessionKey().length()))
                    + "...");
            System.out.println("[CLIENT] → Data (AES): " +
                    encryptedMessage.getEncryptedData().substring(0,
                            Math.min(50, encryptedMessage.getEncryptedData().length()))
                    + "...");

            // ============================================
            // BƯỚC 3: GỬI HYBRID ENCRYPTED DATA TỚI SERVER
            // ============================================
            out.println(encryptedMessage.getEncryptedSessionKey());
            out.println(encryptedMessage.getEncryptedData());
            System.out.println("[CLIENT] ✓ Đã gửi dữ liệu mã hóa tới server");

            // ============================================
            // BƯỚC 4: NHẬN RESPONSE TỪ SERVER (DẠNG AES)
            // ============================================
            String responseEncryptedSessionKey = in.readLine();
            String responseEncryptedData = in.readLine();

            if (responseEncryptedSessionKey == null || responseEncryptedData == null) {
                throw new Exception("Mất kết nối với Server!");
            }

            System.out.println("[CLIENT] ✓ Nhận response mã hóa từ server");

            // ============================================
            // BƯỚC 5: GIẢI MÃ AES RESPONSE (SỬ DỤNG SESSION KEY PLAINTEXT)
            // ============================================
            // Giải mã response data bằng session key plaintext (không cần RSA)
            String decryptedJson = hybridCryptoManager.decryptDataWithAES(responseEncryptedData,
                    sessionKeyPlaintext);
            System.out.println("[CLIENT] ✓ Giải mã AES thành công!");

            // ============================================
            // BƯỚC 6: CHUYỂN ĐỔI JSON THÀNH OBJECT
            // ============================================
            return gson.fromJson(decryptedJson, ResponsePayload.class);
        }

    }

    // =====================================================
    // UDP DISCOVERY
    // =====================================================

    // private InetAddress discoverServer() throws Exception {
    // try (DatagramSocket socket = new DatagramSocket()) {
    // socket.setBroadcast(true);
    // socket.setSoTimeout(TIMEOUT_MS);

    // byte[] sendData = "DISCOVER_SERVER".getBytes(StandardCharsets.UTF_8);

    // socket.send(new DatagramPacket(
    // sendData,
    // sendData.length,
    // InetAddress.getByName("255.255.255.255"),
    // BROADCAST_PORT));

    // byte[] recvBuf = new byte[256];
    // DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

    // // bloking
    // socket.receive(packet);

    // String msg = new String(packet.getData(), 0, packet.getLength());

    // if (!"SERVER_HERE".equals(msg)) {
    // throw new Exception("Invalid discovery response");
    // }

    // System.out.println("[Dicovery]: " + packet.getAddress().toString());
    // return packet.getAddress();
    // }
    // }

    protected static String discoverServerIp() {
        System.out.println("[NETWORK] Searching for Server via UDP Broadcast...");
        try (DatagramSocket socket = new DatagramSocket(BROADCAST_PORT)) {
            socket.setSoTimeout(10000);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                if (message.startsWith("TIKI_SERVER_DISCOVERY")) {
                    String ip = packet.getAddress().getHostAddress();
                    System.out.println("[NETWORK] Server found at: " + ip);
                    return ip;
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Server discovery timed out.");
            return null;
        }
    }
}