package com.example.dt7syntaxcheck.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.dt7syntaxcheck.share.CryptoManager;
import com.example.dt7syntaxcheck.share.RequestPayload;
import com.example.dt7syntaxcheck.share.ResponsePayload;
import com.google.gson.Gson;

public class ClientHandler extends Thread {

    private Socket socket;
    private Gson gson;
    private CryptoManager cryptoManager;
    private String clientIPPrefix;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
        this.cryptoManager = new CryptoManager("MySecretKey123456");
        this.clientIPPrefix = "[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "] ";
    }

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
            // TRẠM 2 + 3: GỌI API & XỬ LÝ (DÙNG PISTON API)
            // ==========================================
            logInfo("Đang đẩy code sang Piston API...");

            com.example.dt7syntaxcheck.server.api.OnlineCompilerAPI api = new com.example.dt7syntaxcheck.server.api.OnlineCompilerAPI();
            com.example.dt7syntaxcheck.server.services.SyntaxChecker checker = new com.example.dt7syntaxcheck.server.services.SyntaxChecker();
            com.example.dt7syntaxcheck.server.services.CodeFormatter formatter = new com.example.dt7syntaxcheck.server.services.CodeFormatter();

            ResponsePayload responsePayload;

            try {
                // 1. Gọi Judge0 API
                String apiResultJson = api.compileAndRun(request.getSourceCode(), request.getLanguageId());
                org.json.JSONObject jsonResponse = new org.json.JSONObject(apiResultJson);

                // 2. Judge0 quy định status ID = 3 là code chạy thành công hoàn toàn
                int statusId = jsonResponse.getJSONObject("status").getInt("id");

                if (statusId == 3) {
                    // --- CODE ĐÚNG SYNTAX ---
                    String output = jsonResponse.optString("stdout", "Chương trình chạy xong nhưng không in ra kết quả (output rỗng).");
                    String formattedCode = formatter.formatCode(request.getSourceCode(), request.getLanguageId());

                    responsePayload = new ResponsePayload(true, output, formattedCode, null);
                    logInfo("Code chuẩn xác! Đã format và lấy output.");
                } else {
                    // --- CODE SAI SYNTAX HOẶC LỖI RUNTIME ---
                    String errorOutput = jsonResponse.optString("compile_output", "");
                    if (errorOutput.isEmpty()) {
                        errorOutput = jsonResponse.optString("stderr", "Lỗi Runtime hoặc lỗi không xác định từ API.");
                    }

                    // Phân tích bóc tách dòng lỗi
                    java.util.List<com.example.dt7syntaxcheck.share.ErrorLog> errorLogs = checker.parseErrors(errorOutput, request.getLanguageId());

                    responsePayload = new ResponsePayload(false, errorOutput, null, errorLogs);
                    logInfo("Phát hiện lỗi cú pháp! Đã bóc tách thành công.");
                }

            } catch (Exception e) {
                logError("Sự cố khi gọi API Judge0: " + e.getMessage());
                responsePayload = new ResponsePayload(false, "Lỗi Server API: " + e.getMessage(), null, null);
            }

            // ==========================================
            // TRẠM 4: ĐÓNG GÓI, MÃ HÓA VÀ TRẢ VỀ
            // ==========================================
            logInfo("Đã có kết quả. Đang mã hóa và gửi trả Client...");
            // ĐÃ FIX LỖI TÊN BIẾN Ở DÒNG NÀY:
            String responseJson = gson.toJson(responsePayload);
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
