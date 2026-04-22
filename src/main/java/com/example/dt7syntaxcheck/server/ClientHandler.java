package com.example.dt7syntaxcheck.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.dt7syntaxcheck.server.api.OnlineCompilerAPI;
import com.example.dt7syntaxcheck.server.services.CodeFormatter;
import com.example.dt7syntaxcheck.server.services.SyntaxChecker;
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
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            logInfo("Luồng xử lý đã mở. Đang chờ Client gửi dữ liệu...");

            String encryptedRequest = in.readLine();
            if (encryptedRequest == null) {
                logInfo("Client đã ngắt kết nối.");
                return;
            }

            String decryptedJson = cryptoManager.decrypt(encryptedRequest);
            RequestPayload request = gson.fromJson(decryptedJson, RequestPayload.class);
            logInfo("Đã nhận code Ngôn ngữ ID: " + request.getLanguageId() + " | Chỉ Format: "
                    + request.isFormatOnly());

            OnlineCompilerAPI api = new OnlineCompilerAPI();
            SyntaxChecker checker = new SyntaxChecker();
            CodeFormatter formatter = new CodeFormatter();

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
            String encryptedResponse = cryptoManager.encrypt(responseJson);
            out.println(encryptedResponse);
            logInfo("Đã trả kết quả về Client!\n");

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
