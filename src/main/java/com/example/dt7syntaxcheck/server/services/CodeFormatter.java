package com.example.dt7syntaxcheck.server.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class CodeFormatter {

    public String formatCode(String rawCode, int languageId) {
        String formatter;

        switch (languageId) {
            case 62: // Java
            case 54: // C++
            case 51: // C#
            case 63: // JavaScript
                formatter = "clangformat";
                break;
            case 71:
            case 92: // Python
                return rawCode;
            default:
                return rawCode;
        }

        return callGodboltAPI(rawCode, formatter, languageId);
    }

    private String callGodboltAPI(String rawCode, String formatter, int languageId) {
        String errorComment = "// [SERVER WARNING]: ";

        try {
            // Khởi tạo kết nối mạng nguyên bản bằng HttpURLConnection
            URL url = new URL("https://godbolt.org/api/format/" + formatter);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // ÉP BUỘC HEADER CHUẨN XÁC TUYỆT ĐỐI (Không cho phép ai tự động chèn charset)
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 LTM-CodeChecker/1.0");
            conn.setDoOutput(true);

            // Đóng gói JSON
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("source", rawCode);
            jsonPayload.put("base", "Google");

            // Viết dữ liệu vào luồng để gửi đi
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            // Đọc phản hồi (Thành công đọc luồng Input, Thất bại đọc luồng Error)
            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            }

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Xử lý JSON trả về
            if (responseCode == 200) {
                JSONObject json = new JSONObject(response.toString());
                if (json.has("answer")) {
                    return json.getString("answer");
                }
            } else {
                System.err.println("[WARNING] API báo lỗi " + responseCode + " - Chi tiết: " + response.toString());
                return errorComment + "Lỗi API (" + responseCode + ") Chi tiết: " + response.toString().replace("\n", " ") + "\n" + rawCode;
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi mạng khi kết nối Godbolt: " + e.getMessage());
            return errorComment + "Sự cố mạng nội bộ Server khi gọi API.\n" + rawCode;
        }

        return rawCode;
    }
}
