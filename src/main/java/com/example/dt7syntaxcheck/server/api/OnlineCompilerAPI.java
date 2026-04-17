package com.example.dt7syntaxcheck.server.api;

import java.io.IOException;
import java.util.Base64;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OnlineCompilerAPI {

    // 1. ĐỔI THÀNH base64_encoded=true theo yêu cầu của Judge0
    private static final String API_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=true&wait=true";

    // API Key của bạn
    private static final String API_KEY = "3e4bbe06admsh1484ccf7193eca1p1d0c1bjsne1e23b175dea";
    private static final String API_HOST = "judge0-ce.p.rapidapi.com";

    private OkHttpClient client;

    public OnlineCompilerAPI() {
        this.client = new OkHttpClient();
    }

    public String compileAndRun(String sourceCode, int languageId) throws IOException {
        JSONObject jsonPayload = new JSONObject();

        // 2. Mã hóa code của Client sang Base64 trước khi gửi đi
        String encodedCode = Base64.getEncoder().encodeToString(sourceCode.getBytes("UTF-8"));
        jsonPayload.put("source_code", encodedCode);
        jsonPayload.put("language_id", languageId);

        RequestBody body = RequestBody.create(
                jsonPayload.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", API_KEY)
                .addHeader("X-RapidAPI-Host", API_HOST)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new IOException("Lỗi gọi Judge0 API: " + response.code() + " - " + responseBody);
            }

            // 3. Dịch ngược kết quả Base64 từ Judge0 về lại String bình thường
            try {
                JSONObject jsonResponse = new JSONObject(responseBody);
                decodeJsonField(jsonResponse, "stdout");
                decodeJsonField(jsonResponse, "stderr");
                decodeJsonField(jsonResponse, "compile_output");
                decodeJsonField(jsonResponse, "message");

                return jsonResponse.toString();

            } catch (Exception e) {
                return responseBody;
            }
        }
    }

    // Hàm tiện ích hỗ trợ dịch ngược Base64 an toàn
    private void decodeJsonField(JSONObject json, String field) {
        if (json.has(field) && !json.isNull(field)) {
            String base64Str = json.getString(field);
            try {
                String decodedStr = new String(Base64.getDecoder().decode(base64Str), "UTF-8");
                json.put(field, decodedStr);
            } catch (Exception ignored) {
            }
        }
    }
}
