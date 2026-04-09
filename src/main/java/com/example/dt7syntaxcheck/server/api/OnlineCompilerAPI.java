package com.example.dt7syntaxcheck.server.api;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class OnlineCompilerAPI {

    // Gọi thẳng vào máy chủ công khai của Piston, KHÔNG CẦN API KEY
    private static final String API_URL = "https://emkc.org/api/v2/piston/execute";
    private OkHttpClient client;

    public OnlineCompilerAPI() {
        this.client = new OkHttpClient();
    }

    public String compileAndRun(String sourceCode, int languageId) throws IOException {
        String language = "";
        String version = "";

        switch (languageId) {
            case 71:
                language = "python";
                version = "3.10.0";
                break;
            case 62:
                language = "java";
                version = "15.0.2";
                break;
            case 54:
                language = "cpp";
                version = "10.2.0";
                break;
            case 63:
                language = "javascript";
                version = "18.15.0";
                break;
            case 51:
                language = "csharp";
                version = "6.12.0";
                break;
            default:
                language = "python";
                version = "3.10.0";
                break;
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("language", language);
        jsonPayload.put("version", version);

        JSONArray filesArray = new JSONArray();
        JSONObject fileObj = new JSONObject();
        fileObj.put("content", sourceCode);
        filesArray.put(fileObj);

        jsonPayload.put("files", filesArray);

        RequestBody body = RequestBody.create(
                jsonPayload.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Lỗi gọi Piston API: " + response.code());
            }
            return response.body().string();
        }
    }
}
