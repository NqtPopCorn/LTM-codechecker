package com.example.dt7syntaxcheck.server;

import com.example.dt7syntaxcheck.share.CryptoManager;
import com.example.dt7syntaxcheck.share.RequestPayload;
import com.example.dt7syntaxcheck.share.ResponsePayload;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Lớp ClientHandler sẽ được tạo ra cho mỗi Client kết nối tới Server. Mỗi ClientHandler chạy trên một luồng riêng biệt để xử lý song song nhiều Client cùng lúc.
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
            // TRẠM 2 + 3: GỌI API & XỬ LÝ (DÙNG PISTON API)
            // ==========================================
            logInfo("Đang đẩy code sang Piston API...");

            com.example.dt7syntaxcheck.server.api.OnlineCompilerAPI api = new com.example.dt7syntaxcheck.server.api.OnlineCompilerAPI();
            com.example.dt7syntaxcheck.server.services.SyntaxChecker checker = new com.example.dt7syntaxcheck.server.services.SyntaxChecker();
            com.example.dt7syntaxcheck.server.services.CodeFormatter formatter = new com.example.dt7syntaxcheck.server.services.CodeFormatter();

            ResponsePayload responsePayload;

            try {
                String apiResultJson = api.compileAndRun(request.getSourceCode(), request.getLanguageId());
                org.json.JSONObject jsonResponse = new org.json.JSONObject(apiResultJson);

                boolean isSuccess = true;
                String output = "";
                String errorOutput = "";

                // Piston chia rạch ròi: "compile" cho lỗi biên dịch (C++, Java), "run" cho lỗi lúc chạy (Python, JS)
                if (jsonResponse.has("compile") && jsonResponse.getJSONObject("compile").getInt("code") != 0) {
                    isSuccess = false;
                    errorOutput = jsonResponse.getJSONObject("compile").getString("stderr");
                } else if (jsonResponse.getJSONObject("run").getInt("code") != 0) {
                    isSuccess = false;
                    errorOutput = jsonResponse.getJSONObject("run").getString("stderr");
                } else {
                    output = jsonResponse.getJSONObject("run").getString("stdout");
                    if (output.trim().isEmpty()) {
                        output = "Chương trình chạy xong nhưng không có output nào được in ra màn hình.";
                    }
                }

                if (isSuccess) {
                    // CODE ĐÚNG
                    String formattedCode = formatter.formatCode(request.getSourceCode(), request.getLanguageId());
                    responsePayload = new ResponsePayload(true, output, formattedCode, null);
                    logInfo("Code chuẩn xác! Đã format và lấy output.");
                } else {
                    // CODE SAI LỖI
                    java.util.List<com.example.dt7syntaxcheck.share.ErrorLog> errorLogs = checker.parseErrors(errorOutput, request.getLanguageId());
                    responsePayload = new ResponsePayload(false, errorOutput, null, errorLogs);
                    logInfo("Phát hiện lỗi! Đã bóc tách thành công.");
                }

            } catch (Exception e) {
                logError("Sự cố khi gọi API Piston: " + e.getMessage());
                responsePayload = new ResponsePayload(false, "Lỗi Server API: " + e.getMessage(), null, null);
            }
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
