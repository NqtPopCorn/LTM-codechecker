package com.example.dt7syntaxcheck.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.dt7syntaxcheck.share.HybridCryptoManager;
import com.example.dt7syntaxcheck.share.RequestPayload;
import com.example.dt7syntaxcheck.share.ResponsePayload;
import com.google.gson.Gson;

public class ClientHandler extends Thread {

    private Socket socket;
    private Gson gson;
    private HybridCryptoManager hybridCryptoManager;
    private String clientIPPrefix;
    private String publicKeyForClient;
    private String privateKeyForServer;

    public ClientHandler(Socket socket, KeyManager.RSAKeyPair rsaKeyPair) {
        this.socket = socket;
        this.gson = new Gson();
        this.publicKeyForClient = rsaKeyPair.publicKey;
        this.privateKeyForServer = rsaKeyPair.privateKey;
        // Khởi tạo HybridCryptoManager với cả public và private key
        this.hybridCryptoManager = new HybridCryptoManager(publicKeyForClient, privateKeyForServer);
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

            logInfo("Luồng xử lý đã mở. Thực hiện Key Exchange...");

            // ============================================
            // BƯỚC 1: HANDSHAKE - GỬI PUBLIC KEY CHO CLIENT
            // ============================================
            out.println(publicKeyForClient);
            logInfo("Đã gửi Public Key cho Client.");

            // ============================================
            // BƯỚC 2: NHẬN YÊU CẦU HYBRID ENCRYPTED TỪ CLIENT
            // ============================================
            String encryptedSessionKey = in.readLine();
            String encryptedData = in.readLine();

            if (encryptedSessionKey == null || encryptedData == null) {
                logInfo("Client đã ngắt kết nối.");
                return;
            }

            // Tạo đối tượng HybridEncryptedMessage từ dữ liệu nhận được
            HybridCryptoManager.HybridEncryptedMessage encryptedMessage
                    = new HybridCryptoManager.HybridEncryptedMessage(encryptedSessionKey, encryptedData);

            // Giải mã hybrid (giải mã RSA key -> giải mã AES data)
            String decryptedJson = hybridCryptoManager.decryptHybrid(encryptedMessage);
            RequestPayload request = gson.fromJson(decryptedJson, RequestPayload.class);
            logInfo("✓ Giải mã Hybrid thành công! Ngôn ngữ ID: " + request.getLanguageId() + " | Chỉ Format: " + request.isFormatOnly());

            // Lưu session key AES từ request để dùng lại cho response
            javax.crypto.SecretKey sessionKey = hybridCryptoManager.decryptSessionKey(encryptedSessionKey);

            com.example.dt7syntaxcheck.server.api.OnlineCompilerAPI api = new com.example.dt7syntaxcheck.server.api.OnlineCompilerAPI();
            com.example.dt7syntaxcheck.server.services.SyntaxChecker checker = new com.example.dt7syntaxcheck.server.services.SyntaxChecker();
            com.example.dt7syntaxcheck.server.services.CodeFormatter formatter = new com.example.dt7syntaxcheck.server.services.CodeFormatter();

            ResponsePayload responsePayload;

            try {
                // ĐỒNG BỘ: KIỂM TRA RẼ NHÁNH TỪ CLIENT YÊU CẦU
                if (request.isFormatOnly()) {
                    // YÊU CẦU 1: CHỈ ĐỊNH DẠNG CODE
                    logInfo("Chuyển code sang API CodeBeautify để Format...");
                    String formattedCode = formatter.formatCode(request.getSourceCode(), request.getLanguageId());
                    responsePayload = new ResponsePayload(true, "Đã định dạng code thành công!", formattedCode, null);

                } else {
                    // YÊU CẦU 2: BIÊN DỊCH VÀ CHẠY CODE BẰNG JUDGE0
                    logInfo("Đang đẩy code sang Judge0 API...");
                    String apiResultJson = api.compileAndRun(request.getSourceCode(), request.getLanguageId());
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(apiResultJson);

                    int statusId = jsonResponse.getJSONObject("status").getInt("id");

                    if (statusId == 3) {
                        // NẾU CODE ĐÚNG -> LẤY OUTPUT -> RỒI GỌI FORMATTER LÀM ĐẸP
                        String output = jsonResponse.optString("stdout", "Chương trình chạy xong (không in output).");
                        String formattedCode = formatter.formatCode(request.getSourceCode(), request.getLanguageId());
                        responsePayload = new ResponsePayload(true, output, formattedCode, null);
                        logInfo("Code chuẩn xác! Đã format và lấy output.");
                    } else {
                        // NẾU CODE LỖI -> KHÔNG FORMAT NỮA -> BÓC TÁCH LỖI
                        String errorOutput = jsonResponse.optString("compile_output", "");
                        if (errorOutput.isEmpty()) {
                            errorOutput = jsonResponse.optString("stderr", "Lỗi Runtime hoặc API.");
                        }

                        java.util.List<com.example.dt7syntaxcheck.share.ErrorLog> errorLogs = checker.parseErrors(errorOutput, request.getLanguageId());
                        responsePayload = new ResponsePayload(false, errorOutput, null, errorLogs);
                        logInfo("Phát hiện lỗi cú pháp! Đã bóc tách.");
                    }
                }

            } catch (Exception e) {
                logError("Sự cố khi xử lý Server: " + e.getMessage());
                responsePayload = new ResponsePayload(false, "Lỗi Server API: " + e.getMessage(), null, null);
            }

            // ============================================
            // BƯỚC 3: GỬI PHẢN HỒI ENCRYPTED VỀ CLIENT
            // (Gửi lại encrypted session key cũ + encrypted data bằng AES)
            // ============================================
            String responseJson = gson.toJson(responsePayload);
            String encryptedResponseData = hybridCryptoManager.encryptDataWithAES(responseJson, sessionKey);
            // Gửi response: encrypted session key (cũ) + encrypted data (AES)
            out.println(encryptedSessionKey);  // Gửi lại session key từ request
            out.println(encryptedResponseData);
            logInfo("✓ Đã mã hóa AES và gửi response về Client!\n");

        } catch (Exception e) {
            logError("Sự cố gián đoạn luồng: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }
}
