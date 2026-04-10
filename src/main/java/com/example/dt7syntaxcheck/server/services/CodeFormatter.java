package com.example.dt7syntaxcheck.server.services;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CodeFormatter {

    public String formatCode(String rawCode, int languageId) {
        String formatter;

        switch (languageId) {
            case 62: // Java
            case 54: // C++
            case 51: // C#
            case 63: // JavaScript
                // Cả 4 ngôn ngữ này đều được hỗ trợ cực tốt bởi công cụ clangformat của Godbolt
                formatter = "clangformat";
                break;
            case 71:
            case 92: // Python
                // Máy chủ Godbolt không cài đặt tool cho Python, ta giữ nguyên code để tránh báo lỗi
                return rawCode;
            default:
                return rawCode;
        }

        return callGodboltAPI(rawCode, formatter, languageId);
    }

    private String callGodboltAPI(String rawCode, String formatter, int languageId) {
        String errorComment = "// [SERVER WARNING]: ";

        try {
            OkHttpClient client = new OkHttpClient();

            // Endpoint chuẩn của Godbolt
            String apiUrl = "https://godbolt.org/api/format/" + formatter;

            // Đóng gói JSON
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("source", rawCode);
            jsonPayload.put("base", "Google"); // Bắt buộc phải khai báo style định dạng

            // Tạo RequestBody 
            RequestBody body = RequestBody.create(
                    jsonPayload.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            // Gắn Request
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    // ĐÂY LÀ DÒNG QUAN TRỌNG NHẤT LÀM NÊN SỰ KHÁC BIỆT:
                    // Ép buộc ghi đè Header để Godbolt không báo lỗi "expected json content" nữa
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Mozilla/5.0 LTM-CodeChecker/1.0")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String respString = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(respString);
                    if (json.has("answer")) {
                        return json.getString("answer");
                    }
                } else {
                    System.err.println("[WARNING] API báo lỗi " + response.code() + " - Chi tiết: " + respString);
                    return errorComment + "Lỗi API (" + response.code() + ") Chi tiết: " + respString.replace("\n", " ") + "\n" + rawCode;
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi mạng khi kết nối Godbolt: " + e.getMessage());
            return errorComment + "Sự cố mạng nội bộ Server khi gọi API định dạng.\n" + rawCode;
        }

        return rawCode;
    }
}
