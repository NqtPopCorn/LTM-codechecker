package com.example.dt7syntaxcheck.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.crypto.SecretKey;

import com.example.dt7syntaxcheck.server.api.OnlineCompilerAPI;
import com.example.dt7syntaxcheck.server.services.CodeFormatter;
import com.example.dt7syntaxcheck.server.services.SyntaxChecker;
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

    private OnlineCompilerAPI api;
    private SyntaxChecker checker;
    private CodeFormatter formatter;

    public ClientHandler(Socket socket, KeyManager.RSAKeyPairs rsaKeyPair) {
        this.socket = socket;
        this.gson = new Gson();
        this.publicKeyForClient = rsaKeyPair.publicKey;
        this.privateKeyForServer = rsaKeyPair.privateKey;
        // Khởi tạo HybridCryptoManager với cả public và private key
        this.hybridCryptoManager = new HybridCryptoManager(publicKeyForClient, privateKeyForServer);
        this.clientIPPrefix = "[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "] ";

        this.api = new OnlineCompilerAPI();
        this.checker = new SyntaxChecker();
        this.formatter = new CodeFormatter();
    }

    private void logInfo(String message) {
        System.out.println(clientIPPrefix + "[INFO] " + message);
    }

    private void logError(String message) {
        System.err.println(clientIPPrefix + "[ERROR] " + message);
    }

    // Implement short-lived socket
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println(
                "[" + threadName + "] Bắt đầu xử lý client: " + socket.getInetAddress().getHostAddress());
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            logInfo("[" + threadName + "] Bắt đầu xử lý...");

            // 1. gửi public key
            out.println(publicKeyForClient);

            logInfo("[" + threadName + "] Đã gửi public key");

            // 2. nhận encrypted key
            String encryptedAES = in.readLine();
            SecretKey sessionKey = hybridCryptoManager.decryptSessionKey(encryptedAES);

            // 3. nhận data
            String encryptedData = in.readLine();
            String decryptedData = hybridCryptoManager.decryptDataWithAES(encryptedData, sessionKey);

            // 4. xử lý
            String response = processData(decryptedData);

            // 5. encrypt response
            String encryptedResponse = hybridCryptoManager.encryptDataWithAES(response, sessionKey);

            // 6. gửi response và key mà client đã gửi
            out.println(encryptedAES);
            out.println(encryptedResponse);

        } catch (Exception e) {
            logError("Sự cố gián đoạn luồng: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    public String processData(String decryptedData) {
        ResponsePayload responsePayload;

        RequestPayload request = gson.fromJson(decryptedData, RequestPayload.class);
        logInfo("✓ Giải mã Hybrid thành công! Ngôn ngữ ID: " + request.getLanguageId() + " | Chỉ Format: "
                + request.isFormatOnly());

        try {
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

                    java.util.List<com.example.dt7syntaxcheck.share.ErrorLog> errorLogs = checker
                            .parseErrors(errorOutput, request.getLanguageId());
                    responsePayload = new ResponsePayload(false, errorOutput, null, errorLogs);
                    logInfo("Phát hiện lỗi cú pháp! Đã bóc tách.");
                }
            }

        } catch (Exception e) {
            logError("Sự cố khi xử lý Server: " + e.getMessage());
            responsePayload = new ResponsePayload(false, "Lỗi Server API: " + e.getMessage(), null, null);
        }

        String responseJson = gson.toJson(responsePayload);
        return responseJson;

    }
}
