package com.example.dt7syntaxcheck.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.dt7syntaxcheck.share.HybridCryptoManager;
import com.example.dt7syntaxcheck.share.RequestPayload;
import com.example.dt7syntaxcheck.share.ResponsePayload;
import com.google.gson.Gson;

public class ClientService {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;

    private HybridCryptoManager hybridCryptoManager;
    private Gson gson;
    private javax.crypto.SecretKey sessionKeyPlaintext;  // Lưu session key plaintext từ request

    public ClientService() {
        this.gson = new Gson();
    }

    /**
     * Hàm này thực hiện: 1. Key Exchange: Nhận public key từ server 2. Mã hóa
     * Hybrid: Tạo session key AES -> Mã hóa RSA -> Mã hóa AES 3. Gửi request và
     * nhận response 4. Giải mã Hybrid response
     */
    public ResponsePayload sendCodeToServer(RequestPayload requestPayload) throws Exception {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT); PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

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
            HybridCryptoManager.HybridEncryptedMessageWithKey encryptedMessage = hybridCryptoManager.encryptHybridWithKey(jsonPayload);
            this.sessionKeyPlaintext = encryptedMessage.getSessionKey();  // LƯU SESSION KEY
            System.out.println("[CLIENT] ✓ Mã hóa Hybrid thành công!");
            System.out.println("[CLIENT] → Session Key (RSA): " + encryptedMessage.getEncryptedSessionKey().substring(0, Math.min(50, encryptedMessage.getEncryptedSessionKey().length())) + "...");
            System.out.println("[CLIENT] → Data (AES): " + encryptedMessage.getEncryptedData().substring(0, Math.min(50, encryptedMessage.getEncryptedData().length())) + "...");

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
            String decryptedJson = hybridCryptoManager.decryptDataWithAES(responseEncryptedData, sessionKeyPlaintext);
            System.out.println("[CLIENT] ✓ Giải mã AES thành công!");
            System.out.println("[CLIENT] ✓ Giải mã AES thành công!");

            // ============================================
            // BƯỚC 6: CHUYỂN ĐỔI JSON THÀNH OBJECT
            // ============================================
            return gson.fromJson(decryptedJson, ResponsePayload.class);
        }
    }
}
