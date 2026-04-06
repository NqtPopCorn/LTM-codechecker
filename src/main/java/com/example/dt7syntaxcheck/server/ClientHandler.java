package com.example.dt7syntaxcheck.server;

import com.example.dt7syntaxcheck.share.CryptoManager;
import com.example.dt7syntaxcheck.share.RequestPayload;
import com.example.dt7syntaxcheck.share.ResponsePayload;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private Gson gson;
    private CryptoManager cryptoManager;
    private String clientIPPrefix; // Dùng để định danh log của từng client

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
        // Dùng chung một Key với Client để có thể mở khóa được tin nhắn
        this.cryptoManager = new CryptoManager("MySecretKey123456");

        // Tạo chuỗi tiền tố [IP:Port] để in ra màn hình dễ theo dõi
        this.clientIPPrefix = "[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "] ";
    }

    // --- CÔNG CỤ DEBUG / LOGGING ---
    private void logInfo(String message) {
        System.out.println(clientIPPrefix + "[INFO] " + message);
    }

    private void logError(String message) {
        System.err.println(clientIPPrefix + "[ERROR] " + message);
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            logInfo("Luồng xử lý đã mở. Đang chờ Client gửi dữ liệu...");

            // ==========================================
            // TRẠM 1: NHẬN VÀ GIẢI MÃ
            // ==========================================
            String encryptedRequest = in.readLine();
            if (encryptedRequest == null) {
                logInfo("Client đã ngắt kết nối trước khi gửi dữ liệu.");
                return;
            }
            logInfo("Đã nhận được gói tin mã hóa. Tiến hành giải mã...");

            String decryptedJson = cryptoManager.decrypt(encryptedRequest);
            RequestPayload request = gson.fromJson(decryptedJson, RequestPayload.class);
            logInfo("Giải mã thành công! Nhận code thuộc Ngôn ngữ ID: " + request.getLanguageId());

            // ==========================================
            // TRẠM 2 + 3: GỌI API & XỬ LÝ (Sẽ code ở phần sau)
            // ==========================================
            logInfo("Đang đẩy code sang OnlineCompiler API...");
            // TODO: Gọi API biên dịch
            // TODO: Format code hoặc Phân tích lỗi

            // Tạm thời tạo Dữ liệu giả (Mock) để test kết nối từ Client -> Server -> Client
            Thread.sleep(1500); // Giả lập độ trễ mạng 1.5 giây
            ResponsePayload response = new ResponsePayload(
                    true,
                    "Kết nối Server thành công!\nĐoạn code của bạn dài " + request.getSourceCode().length() + " ký tự.",
                    "// Chỗ này sau này sẽ chứa code đã được format",
                    null
            );

            // ==========================================
            // TRẠM 4: ĐÓNG GÓI, MÃ HÓA VÀ TRẢ VỀ
            // ==========================================
            logInfo("Đã có kết quả. Đang mã hóa và gửi trả Client...");
            String responseJson = gson.toJson(response);
            String encryptedResponse = cryptoManager.encrypt(responseJson);

            out.println(encryptedResponse);
            logInfo("Đã gửi thành công!");

        } catch (Exception e) {
            logError("Sự cố gián đoạn luồng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                logInfo("Đã đóng Socket và giải phóng tài nguyên luồng.\n");
            } catch (Exception e) {
                logError("Lỗi khi đóng Socket: " + e.getMessage());
            }
        }
    }
}
