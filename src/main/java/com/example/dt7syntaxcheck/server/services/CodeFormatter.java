package com.example.dt7syntaxcheck.server.services;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CodeFormatter {

    public String formatCode(String rawCode, int languageId) {
        if (languageId == 71) {
            return rawCode;
        }

        String langName = "";
        switch (languageId) {
            case 62:
                langName = "Java";
                break;
            case 54:
                langName = "C++";
                break;
            case 51:
                langName = "C#";
                break;
            case 63:
                langName = "JavaScript";
                break;
        }

        return callFormatterAPI(rawCode, langName);
    }

    private String callFormatterAPI(String rawCode, String lang) {
        try {
            OkHttpClient client = new OkHttpClient();

            String apiUrl = "https://codebeautify.org/api/format";

            String mode = "javascript";
            if (lang.equals("Java") || lang.equals("C++") || lang.equals("C#")) {
                mode = "c_cpp";
            }

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
                        if (json.has("result")) {
                            return json.getString("result");
                        }
                        if (json.has("formatted_code")) {
                            return json.getString("formatted_code");
                        }
                    } catch (Exception e) {
                        return respString;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[WARNING] Lỗi gọi API Formatter, chuyển sang dùng Format Basic: " + e.getMessage());
        }

        return formatCodeBasic(rawCode);
    }

    // ĐÃ KHÔI PHỤC HÀM BACKUP DƯỚI ĐÂY
    private String formatCodeBasic(String rawCode) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        String tab = "    ";

        String cleanedCode = rawCode.replaceAll("\\s+", " ").replace("{", "{\n").replace("}", "}\n").replace(";", ";\n");
        String[] lines = cleanedCode.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("}")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            for (int i = 0; i < indentLevel; i++) {
                formatted.append(tab);
            }
            formatted.append(line).append("\n");

            if (line.endsWith("{")) {
                indentLevel++;
            }
        }
        return formatted.toString();
    }
}
