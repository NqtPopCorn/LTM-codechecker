package com.example.dt7syntaxcheck.server.services;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CodeFormatter {

    public String formatCode(String rawCode, int languageId) {
        String mode = "javascript"; // Mặc định

        // Ánh xạ chính xác ID ngôn ngữ sang Mode của API CodeBeautify
        switch (languageId) {
            case 62: // Java
                mode = "java";
                break;
            case 54: // C++
                mode = "c_cpp";
                break;
            case 51: // C#
                mode = "csharp";
                break;
            case 63: // JavaScript
                mode = "javascript";
                break;
            case 71: // Python (Bản cũ)
            case 92: // Python 3.11 (Bản mới)
                mode = "python";
                break;
            default:
                return rawCode; // Nếu ngôn ngữ lạ, trả về code gốc
        }

        return callFormatterAPI(rawCode, mode, languageId);
    }

    private String callFormatterAPI(String rawCode, String mode, int languageId) {
        try {
            OkHttpClient client = new OkHttpClient();

            // Sử dụng API định dạng chuẩn
            String apiUrl = "https://codebeautify.org/api/format";

            RequestBody body = new FormBody.Builder()
                    .add("code", rawCode)
                    .add("mode", mode)
                    .build();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String respString = response.body().string();

                    try {
                        JSONObject json = new JSONObject(respString);
                        // Tùy theo ngôn ngữ mà API trả về khóa result hoặc formatted_code
                        if (json.has("result")) {
                            return json.getString("result");
                        }
                        if (json.has("formatted_code")) {
                            return json.getString("formatted_code");
                        }
                    } catch (Exception e) {
                        // Nếu dữ liệu trả về không phải JSON mà là String trơn
                        return respString;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[WARNING] Lỗi gọi API Formatter, trả về code gốc: " + e.getMessage());
        }

        // Nếu API sập, trả về code gốc
        return rawCode;
    }
}
