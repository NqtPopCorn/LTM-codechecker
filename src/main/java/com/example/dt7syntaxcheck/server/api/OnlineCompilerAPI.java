package com.example.dt7syntaxcheck.server.api;

import java.io.IOException;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OnlineCompilerAPI {

    // Ép API chờ chạy xong mới trả kết quả (wait=true)
    private static final String API_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=false&wait=true";

    // TODO: THAY CHUỖI NÀY BẰNG API KEY RAPIDAPI CỦA BẠN
    private static final String API_KEY = "3e4bbe06admsh1484ccf7193eca1p1d0c1bjsne1e23b175dea";
    private static final String API_HOST = "judge0-ce.p.rapidapi.com";

    private OkHttpClient client;

    public OnlineCompilerAPI() {
        this.client = new OkHttpClient();
    }

    public String compileAndRun(String sourceCode, int languageId) throws IOException {
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("source_code", sourceCode);
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
            if (!response.isSuccessful()) {
                throw new IOException("Lỗi gọi Judge0 API: " + response.code() + " - " + response.message());
            }
            return response.body().string();
        }
    }
}
