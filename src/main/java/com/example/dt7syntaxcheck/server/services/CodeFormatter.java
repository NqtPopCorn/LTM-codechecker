package com.example.dt7syntaxcheck.server.services;

import okhttp3.*;
import org.json.JSONObject;

public class CodeFormatter {

    /**
     * Hàm chính được gọi từ ClientHandler
     */
    public String formatCode(String rawCode, int languageId) {
        // Python (71) dùng khoảng trắng để thụt lề, không thể tự auto-format một cách an toàn bằng API này
        if (languageId == 71) {
            return rawCode; 
        }

        // Chuyển ID ngôn ngữ sang tên ngôn ngữ
        String langName = "";
        switch (languageId) {
            case 62: langName = "Java"; break;
            case 54: langName = "C++"; break;
            case 51: langName = "C#"; break;
            case 63: langName = "JavaScript"; break;
        }

        // Gọi API Formatter
        return callFormatterAPI(rawCode, langName);
    }

    /**
     * Hàm gọi API bên thứ 3 (CodeBeautify) để format code chuẩn công nghiệp
     */
    private String callFormatterAPI(String rawCode, String lang) {
        try {
            OkHttpClient client = new OkHttpClient();
            
            // Sử dụng API công khai của codebeautify
            String apiUrl = "https://codebeautify.org/api/format";
            
            // Map ngôn ngữ của chúng ta sang Mode mà API này hiểu
            String mode = "javascript";
            if (lang.equals("Java") || lang.equals("C++") || lang.equals("C#")) {
                mode = "c_cpp";
            }

            // Tạo Body HTTP POST
            RequestBody body = new FormBody.Builder()
                    .add("code", rawCode)
                    .add("mode", mode)
                    .build();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();

            // Thực thi Request
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String respString = response.body().string();
                    
                    // Thử đọc dưới dạng JSON (Một số API trả về {"result": "code..."})
                    try {
                        JSONObject json = new JSONObject(respString);
                        if (json.has("result")) return json.getString("result");
                        if (json.has("formatted_code")) return json.getString("formatted_code");
                    } catch (Exception e) {
                        // Nếu không phải JSON, nghĩa là nó trả thẳng đoạn code dưới dạng String trơn
                        return respString;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[WARNING] Lỗi gọi API Formatter, chuyển sang dùng Format Basic: " + e.getMessage());
        }
        
        // Nếu API sập hoặc mất mạng, gọi hàm backup (Fallback)
        return formatCodeBasic(rawCode); 
    }

  
}